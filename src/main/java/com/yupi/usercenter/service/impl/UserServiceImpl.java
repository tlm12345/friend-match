package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.usercenter.common.AlgorithmUtils;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.mapper.UserMapper;
import jodd.util.collection.MapEntry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.yupi.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource(name = "getRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    // https://www.code-nav.cn/

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    // [加入星球](https://www.code-nav.cn/) 从 0 到 1 项目实战，经验拉满！10+ 原创项目手把手教程、7 日项目提升训练营、60+ 编程经验分享直播、1000+ 项目经验笔记

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误!");
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签列表查询用户
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList, int queryType) {
        if (queryType == 0) return searchUserByTagsInDB(tagNameList);
        if (queryType == 1) return searchUserByTagsInMM(tagNameList);
        return Arrays.asList();
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public Integer updateUser(User user, User currentUser) {
        long userId = user.getId();
        if (!isAdmin(currentUser) && userId != currentUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        User oldUser = userMapper.selectById(userId);
        if (oldUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        return userMapper.updateById(user);
    }

    @Override
    public List<User> getRecommendUser(long num, User loginUser) {
//        1. 校验用户请求参数
        if (num <= 0 || num > 20) throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求推荐用户数量不合规(0~20)");

        // 查询缓存，如果没有，再进行实际的业务查询
        String recommendInfoKey = String.format("friendMatch:user:recommend:%s:%d", loginUser.getId(), num);
        ValueOperations<String, Object> stringObjectValueOperations = redisTemplate.opsForValue();

        Object o = stringObjectValueOperations.get(recommendInfoKey);
        // cache hint
        if (o != null){
            return (List<User>) o;
        }

//        2. 获取用户的tags信息。查询数据库，获取所有其他用户的tags信息
        Long userId = loginUser.getId();
        String tagsB = loginUser.getTags();
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> tagsBList = new Gson().fromJson(tagsB,type);
        if (CollectionUtils.isEmpty(tagsBList)) throw new BusinessException(ErrorCode.NULL_ERROR, "用户没有标签信息");

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id", "tags");
        // 剔除没有标签的用户（这个剔除逻辑不是特别健壮，所以后面仍需要判断，用户的标签是否为空）
        userQueryWrapper.like("tags", "[_%]");
        List<User> userList = this.list(userQueryWrapper);
//        3. 依据算法计算相关性，取前k个最相关的推荐用户。
        HashMap<Long, Integer> unOrderedEditDisMap = (HashMap<Long, Integer>) userList.stream().filter(item -> {
            if (item.getId().equals(userId)) return false;
            String tagsA = item.getTags();
            List<String> tagsAList = new Gson().fromJson(tagsA, type);
            if (CollectionUtils.isEmpty(tagsAList)) return false;
            return true;
        }).collect(Collectors.toMap(User::getId, item -> {
            String tagsA = item.getTags();
            List<String> tagsAList = new Gson().fromJson(tagsA, type);
            int editDis = AlgorithmUtils.minDistance(tagsAList, tagsBList);
            return editDis;
        }));
        ArrayList<Map.Entry<Long, Integer>> orderingEditDisList = new ArrayList<>(unOrderedEditDisMap.entrySet());
        orderingEditDisList.sort(Comparator.comparingInt(Map.Entry::getValue));

//        4. 返回推荐用户
        List<User> res = new ArrayList<>();
        for(int i = 0; i< num; i++){
            if (i >= orderingEditDisList.size()){
                break;
            }
            Map.Entry<Long, Integer> entry = orderingEditDisList.get(i);
            Long id = entry.getKey();
            User u = this.getById(id);
            User safetyUser = this.getSafetyUser(u);
            res.add(safetyUser);
        }

        return res;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {

        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (attribute == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        User oldUser = (User) attribute;
        User user = this.getById(oldUser.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return user;
    }


    /**
     * 利用sql在数据库进行模糊查询
     * @param tagNameList
     * @return
     */
    public List<User> searchUserByTagsInDB(List<String> tagNameList){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        for (String tag : tagNameList) {
            wrapper.like("tags", tag);
        }

        List<User> users = userMapper.selectList(wrapper);
        return users;
    }

    /**
     * 在内存中筛选
     * @param tagNameList
     * @return
     */
    public List<User> searchUserByTagsInMM(List<String> tagNameList){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        List<User> users = userMapper.selectList(wrapper);

        return users.stream().filter(user->{
            if (user == null) return false;
            String tagListString = user.getTags();
            List<String> tempTagNameList = new Gson().fromJson(tagListString, new TypeToken<List<String>>() {
            }.getType());

            for (String tagName : tagNameList) {
                if (!tempTagNameList.contains(tagName)) return false;
            }
            return true;
        }).collect(Collectors.toList());

    }

}

// [加入我们](https://yupi.icu) 从 0 到 1 项目实战，经验拉满！10+ 原创项目手把手教程、7 日项目提升训练营、1000+ 项目经验笔记、60+ 编程经验分享直播