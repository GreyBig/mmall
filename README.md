## mmall

从0开始 独立完成企业级Java电商网站服务端开发

### 包结构
pojo：数据库表的实体类包
dao：Dao层接口的包
vo：调用Dao层接口实现一定逻辑供service层调用
service：业务逻辑层
controller：springmvc 视图层
common：公共类的包
util：工具类包

dao 最下面与数据库交互→service层→control层
web.xml是dao层的实现

mybatis-generator配置：generatorConfig.xml
数据源属性配置: datasource.properties
IDEA右边Maven的mybatis-generator:generate就能生成dao、pojo、mappers，mappers是dao的实现

四个Spring官方Demo：
1/spring-mvc-showcase
2/spring-petclinic
3/greenhouse
4/spring-boot

两个提高工作效率的神器Restlet Client和fe助手
json格式化工具——fe助手

IDEA代码提示快捷键：默认为ctrl+空格，但与输入法冲突，可在Keymap中修改为alt+/