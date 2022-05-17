[toc]

## 用户模块

### 主要功能

登录、用户名验证、注册、忘记密码、提交问题答案、充值密码、获取用户信息、更新用户信息、退出登录

### 学习要点

* 横向越权、纵向越权安全漏洞
* MD5铭文加密及增加salt值
* Guava缓存的使用
* 高服用服务相应对象的设计思想及抽象封装
* Session的使用

### 横向越权、纵向越权安全漏洞

* 横向越权：攻击者尝试访问与他拥有相同权限的用户的资源

  ```xml
    <!-- 查询时用count(1) 这里一定要加id字段，不然就可以用字典不断试密码 -->
  <select id="checkPassword" resultType="int" parameterType="map">
      SELECT
        count(1)
      from mmall_user
      where password = #{password}
        and id = #{userId} 
    </select>
  ```
* 纵向越权：低级别攻击者尝试访问高级别用户的资源：检查是否是管理员才可操作某些权限

### 创建高复用的服务端响应泛型类

### Guava是什么？

Guava是一种基于开源的Java库，Google Guava源于2007年的"Google Collections Library"。这个库是为了方便编码，并减少编码错误。这个库用于提供集合，缓存，支持原语句，并发性，常见注解，字符串处理，I/O和验证的实用方法。

### Guava缓存的使用

使用Guava缓存Token，Token是在回答问题重置密码正确后生成的，修改密码时再验证Token

```java
public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);
    public static final String TOKEN_PREFIX = "token_";
    //LRU算法
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    public static void setKey(String key,String value){
        localCache.put(key,value);
    }
    public static String getKey(String key){
        String value = null;
        try {
            value = localCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error",e);
        }
        return null;
    }
}
```

## 分类模块

### 功能介绍：

* 获取节点
* 增加节点
* 修改名字
* 获取分类ID
* 递归子节点ID

### 学习目标

* **如何设计及封装无限层级的树状数据结构**

  分类表中的id为当前分类id，parent_id为父类别id，当parent_id为0时说明是根节点，一级级别
* **递归算法的设计思想**
* **如何处理复杂对象排重**
* **重写hashcode和equal的注意事项**

  使用Set方法可以排重使用Set集合存储对象时，`equals`和 `hashCode`方法都要重写

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

## 后台商品管理模块

### 后台功能

* 商品列表 、商品搜索、图片上传、富文本上传、商品详情、商品上下架、增加商品、更新商品

### 学习目标

* FTP服务的对接
* SringMVC文件上传
* 流读取Properties配置文件
* 抽象POJO、BO、VO对象直接的转换关系及解决思路
* joda-time快速入门
* 静态块
* Myabtis-PageHelper高效准确地分页及动态排序
* Mybatis对List遍历的实现方法
* Mybatis对where语句动态拼接的几个版本

这个模块的接口都要判断用户是否登录，因为是后台所以还要验证是否是管理员

#### 保存或更新产品

1. 如果子图第一个不为空 就将第一个复制给主图
2. 判断有无产品ID，如果有就更新产品，没有新增产品

#### 设置产品销售状态

1. 产品ID和状态都不能为空
2. 再使用Mapper的选择性更新 updateByPrimaryKeySelective

#### 产品详情

1. 根据产品ID查询产品，所以产品ID不能为空

#### 产品list

1. 使用PageHelper插件做分页，需要传入pageNum，pageSize
2. 因为不需要Product的所有数据，所以用ProductListVo接收需要展示的数据

   ```java
           //startPage--start
           //填充自己的sql查询逻辑
           //pageHelper-收尾
           PageHelper.startPage(pageNum,pageSize);

           List<Product> productList = productMapper.selectList();
   		// Lists是guava的
           List<ProductListVo> productListVoList = Lists.newArrayList();
           for(Product productItem : productList){
               ProductListVo productListVo = assembleProductListVo(productItem);
               productListVoList.add(productListVo);
           }
           PageInfo pageResult = new PageInfo(productList);
           pageResult.setList(productListVoList);
           return ServerResponse.createBySuccess(pageResult);
   ```

   **productMapper.selectList()看起来是一次取全部数据，但事实上不是：**

   pageHelper分页主要是通过 aop来实现，在执行sql之前会在sql语句中添加limit offset这两个参数。这样就完成了动态的分页。

#### 产品搜索

1. 参数productName、productId、pageNum(default=1)、pageSize(default=10)
2. pageNum与pageSize是给PageHelper做分页用
3. 模糊匹配产品名称

   ```java
   if(StringUtils.isNotBlank(productName)){
       productName = new StringBuilder().append("%").append(productName).append("%").toString();
   }
   List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);
   ...... // 后面分页和产品列表一样
   ```
4. mapper

   ```xml
   <select id="selectByNameAndProductId" resultMap="BaseResultMap" parameterType="map">
     SELECT
     <include refid="Base_Column_List"/>
     from mmall_product
     <where>
       <if test="productName != null">
         and name like #{productName}
       </if>
       <if test="productId != null">
         and id = #{productId}
       </if>
     </where>
   </select>
   ```

#### 图片上传

Controller中的upload方法

```java
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session,@RequestParam(
        value = "upload_file",required = false) MultipartFile file,
                                 HttpServletRequest request){
		......// 验证是否登录
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 设置上传文件的保存地址目录
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
```

FileServiceImpl

```java
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    public String upload(MultipartFile file,String path){
        String fileName = file.getOriginalFilename();
        //扩展名
        //abc.jpg
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);
        try {
            file.transferTo(targetFile);
            //文件已经上传成功了
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //已经上传到ftp服务器上
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        //A:abc.jpg
        //B:abc.jpg
        return targetFile.getName();
    }

}
```

FTPUtil工具

```java
public class FTPUtil {
    private static  final Logger logger = LoggerFactory.getLogger(FTPUtil.class);
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");
    public FTPUtil(String ip, int port, String user, String pwd){
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser,ftpPass);
        logger.info("开始连接ftp服务器");
        boolean result = ftpUtil.uploadFile("img",fileList);
        logger.info("开始连接ftp服务器,结束上传,上传结果:{}");
        return result;
    }
    private boolean uploadFile(String remotePath,List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        //连接FTP服务器
        if(connectServer(this.ip,this.port,this.user,this.pwd)){
            try {
                ftpClient.changeWorkingDirectory(remotePath);// 切换文件夹
                ftpClient.setBufferSize(1024); // 设置缓冲区
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); //文件类型设置为二进制类型，防止乱码
                ftpClient.enterLocalPassiveMode(); // 打开本地被动模式
                for(File fileItem : fileList){  // 正式上传
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(),fis);
                }
            } catch (IOException e) {
                logger.error("上传文件异常",e);
                uploaded = false;
                e.printStackTrace();
            } finally {
                fis.close();
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }
    private boolean connectServer(String ip,int port,String user,String pwd){
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user,pwd);
        } catch (IOException e) {
            logger.error("连接FTP服务器异常",e);
        }
        return isSuccess;
    }
    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;
	,,,,,,// get和set方法
}
```

#### SpringMVC上传文件的配置

WEB-INF下的dispatcher-servlet.xml

```xml
    <!-- 文件上传 -->
    <bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <property name="maxUploadSize" value="10485760"/> <!-- 10m -->
        <property name="maxInMemorySize" value="4096" />
        <property name="defaultEncoding" value="UTF-8"></property>
    </bean>
```

再配合MultipartFile完成上传工作

#### 富文本上传图片

富文本用的simditor

```java
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap = Maps.newHashMap();
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("success",false);
            resultMap.put("msg","请登录管理员");
            return resultMap;
        }
        //富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
//        {
//            "success": true/false,
//                "msg": "error message", # optional
//            "file_path": "[real file path]"
//        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            // 返回的header是富文本要求的
            response.addHeader("Access-Control-Allow-Headers","X-File-Name"); 
            return resultMap;
        }else{
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }
```

## 前台商品管理模块

### 前台功能

* 产品搜索、动态排序列表、产品详情

#### 商品详情

1. productId不能为空
2. 如果查询出来的产品为空，则是下架或删除
3. 判断产品的status，如果不为1则是下架或删除
4. 用assembleProductDetailVo方法将product存入productDetailVo

#### 产品搜索及动态排序List

## 购物车模块

### 功能介绍：

* 加入商品、更新商品数、查询商品数、移除商品、单选/取消、全选/取消、购物车列表

### 学习目标

* 购物车模块的设计思想
* 如何封装一个高复用购物车核心方法
* 解决浮点型商业运算中丢失精度的问题

  * 一定要用BigDecimal的String构造器

    ```java
    public class BigDecimalUtil {
        private BigDecimalUtil(){}
        public static BigDecimal add(double v1,double v2){
            BigDecimal b1 = new BigDecimal(Double.toString(v1));
            BigDecimal b2 = new BigDecimal(Double.toString(v2));
            return b1.add(b2);
        }
        public static BigDecimal sub(double v1,double v2){
            BigDecimal b1 = new BigDecimal(Double.toString(v1));
            BigDecimal b2 = new BigDecimal(Double.toString(v2));
            return b1.subtract(b2);
        }
        public static BigDecimal mul(double v1,double v2){
            BigDecimal b1 = new BigDecimal(Double.toString(v1));
            BigDecimal b2 = new BigDecimal(Double.toString(v2));
            return b1.multiply(b2);
        }
        public static BigDecimal div(double v1,double v2){
            BigDecimal b1 = new BigDecimal(Double.toString(v1));
            BigDecimal b2 = new BigDecimal(Double.toString(v2));
            return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);//四舍五入,保留2位小数
            //除不尽的情况
        }
    }
    ```

#### 添加购物车

控制器add方法：

1. 参数有sesssion,count,productId
2. 从session拿取用户信息，如果user为null则返回需要登录。
3. 如果不为null就调用iCartService.add(user.getId(),productId,count)方法

业务层add方法

1. 如果productId或count为null，返回参数异常
2. 根据userId和productId查询购物车
3. 如果这个产品不在这个购物车里,需要新增一个这个产品的记录
4. 如果这个产品已经在购物车里了，和原本的数量相加

封装了一个购物车产品的VO和购物车VO

## 收货地址管理

### 功能介绍：

* 添加地址、删除地址、更新地址、地址列表、地址分页、地址详情

### 学习目标

* SpringMVC数据绑定中对象绑定
* mybatis自动生成主键、配置和使用
* 如何避免横向越权漏洞的顽固
