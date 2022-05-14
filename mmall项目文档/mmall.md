## 用户模块

**主要功能**

登录、用户名验证、注册、忘记密码、提交问题答案、充值密码、获取用户信息、更新用户信息、退出登录

**学习要点**

* 横向越权、纵向越权安全漏洞
* MD5铭文加密及增加salt值
* Guava缓存的使用
* 高服用服务相应对象的设计思想及抽象封装
* Session的使用



**横向越权、纵向越权安全漏洞**

* 横向越权：攻击者尝试访问与他拥有相同权限的用户的资源

  ```sql
    <select id="checkPassword" resultType="int" parameterType="map">
      SELECT
        count(1)
      from mmall_user
      where password = #{password}
        and id = #{userId} 
    </select>
    <!-- 查询时用count(1) 这里一定要加id字段，不然就可以用字典不断试密码 -->
  
  ```

  

* 纵向越权：低级别攻击者尝试访问高级别用户的资源



##### 创建高复用的服务端响应泛型类



### Guava是什么？

Guava是一种基于开源的Java库，Google Guava源于2007年的"Google Collections Library"。这个库是为了方便编码，并减少编码错误。这个库用于提供集合，缓存，支持原语句，并发性，常见注解，字符串处理，I/O和验证的实用方法。

## 分类模块

#### 功能介绍：

* 获取节点
* 增加节点
* 修改名字
* 获取分类ID
* 递归子节点ID

#### 学习目标

* **如何设计及封装无限层级的树状数据结构**

  分类表中的id为当前分类id，parent_id为父类别id，当parent_id为0时说明是根节点，一级级别

* **递归算法的设计思想**

* **如何处理复杂对象排重**

* **重写hashcode和equal的注意事项** 

  使用Set方法可以排重使用Set集合存储对象时，`equals`和`hashCode`方法都要重写

**递归查询本节点的id及孩子节点的id**

```java
    /**
     * 递归查询本节点的id及孩子节点的id
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,categoryId);
        List<Integer> categoryIdList = Lists.newArrayList();
        if(categoryId != null){
            for(Category categoryItem : categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }
    //递归算法,算出子节点
    private Set<Category> findChildCategory(Set<Category> categorySet ,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        //查找子节点,递归算法一定要有一个退出的条件
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for(Category categoryItem : categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }

```

