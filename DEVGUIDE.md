# Apollo配置中心使用说明
## Apollo基本介绍
一般情况下，有三个环境就足够了  
1. DEV: 一般就是本地开发环境
2. TEST: 测试环境
3. PRO: 生产环境 

当然，我们实际应用中，可能会在每个环境中部署很多套环境，每套环境又想使用不同的配置，可能存在下面一些场景：  
1. 测试环境，多人协作开发，多个分支，可能会部署多套测试环境，针对这种情况，我们可以使用 namespace 的方式来划分不同的环境
2. 正式环境，如果我们的应用需要部署多个机房，每个机房的配置一般差异化比较大，这个时候我们可以使用集群的概念，一个机房对应一个集群，可以在 PRO 下面建立多个集群实现

上面的场景，我在后续的文档中会详细介绍一些推荐使用的方案，这里先不进一步描述。

## Hello World
程序员最喜欢HelloWorld了，这里不做更多介绍，还是先搞一个 HelloWorld 来体验下，因为目前大多数都是 SpringBoot 项目，这里就拿 SpringBoot 介绍一个HelloWorld。

[apollo-client-spring-boot-starter](apollo-client-spring-boot-starter) 中也有单元测试可以看具体效果，建议看看这个单元测试！


### 基于SpringBoot的HelloWorld
要使用 Apollo 配置中心，主要有几个步骤：
1. Apollo 管理后台新建一个应用  
    
    ![alt text](doc/images/create-app-entry.png)  
    ![alt text](doc/images/create-app.png)
    
    这里我们建议应用的ID可以使用你的项目+模块名称
    
2. SpringBoot 应用引入以下 Jar 包：
    ```xml
    <dependency>
        <groupId>apollo</groupId>
        <artifactId>apollo-client-spring-boot-starter</artifactId>
        <version>1.5.0-SNAPSHOT</version>
    </dependency>
    ```
3. <code>application.properties</code> 配置Apollo相关属性
    至少要配置一个 apollo 上的应用ID和使用Apollo上的什么环境
    ```properties
    # Apollo 新建的时候会有一个 应用ID，直接复制过来
    apollo.appId=SampleApp
    # 只要使用哪个环境的配置，目前只需的 Apollo 仅支持 DEV,TEST,PRO 三个环境
    apollo.env=DEV
    ```
    其他配置项说明： 
    
    |**配置项**|**示例**|**说明**|
    |:-------|:-------|:-------|
    |apollo.bootstrap.enabled|apollo.bootstrap.enabled=true|默认是true, 即是否启动的时候加载指定的Apollo中指定namespace的配置到Spring环境中|
    |apollo.bootstrap.namespaces|apollo.bootstrap.namespaces=application|配置启动时候要加载到Spring环境中的配置命名空间，使用英文逗号分隔多个, apollo.bootstrap.enabled=true 的时候才生效|
    |apollo.cacheDir|apollo.cacheDir=/data/apollo/xxx|pollo 本地缓存配置目录,windows默认是/data/${app.id}, linux默认是 /data/apollo/${app.id}|
    |apollo.cluster|apollo.cluster=default|集群配置，即当前环境使用什么集群的配置，默认是没有集群的，需要管理后台配置集群，通常是不同机房或数据中心才需要集群概念|
    |apollo.meta|apollo.meta=http://127.0.0.1:8081/|指定特定的 ApolloConfigService 的服务地址，表示当前使用的实际环境，这个一般不用配置，需要部署了特定环境 Apollo ConfigService 和对应 portal才有意义|
    
4. 代码中 <code>@Value</code> 使用Apollo变量
    上面都做了之后，你可以使用 <code>@Value</code> 来引入变量了，举些个例子： 
    - <code>@Value("${timeout}") private long timeout;</code> 表示引用属性 timeout
    - <code>@Value("#{'${ids}'.split(',')}") private List<String> ids;</code> 允许使用EL表达式来对值进行处理
    
    注意了，使用 <cde>@Value</code> 的方式，在 Apollo 控制台变更并发布配置后，对应的属性也会发生变更，而不需要重新部署程序 

5. 编程式使用Apollo变量 使用 Config 接口来读取相关的配置了
   ```java
   // 直接使用 getAppConfig, 获取到的是 application 这个namespace的配置
   private Config config = ConfigService.getAppConfig();
   // 直接指定某个namespace来获取配置
   private Config config = ConfigService.getConfig("application");
   ```

6. 使用对象接收配置
    我们除了使用 <code>@Value</code> 可以使用Apollo的配置外，还可以使用对象 Bean 的方式来使用 Apollo 的配置。  
    
    **定义配置Bean：**
    
    ```java
    @ConfigurationProperties(prefix = "apollo.config")
    @ApolloConfigBean
    @RefreshScope
    public class ApolloConfig {
    
        private int expireSeconds;
        private String clusterNodes;
        private int commandTimeout;
    
        private Map<String, String> someMap;
        private List<String> someList;
        // 省略 get/set
    }
    ```
    注意，上面说到的注解都是需要的，看起来是麻烦点, 但是上面三个是必要的，<code>@ConfigurationProperties(prefix = "apollo.config")</code> 指定具体配置前缀， <code>@ApolloConfigBean</code> 标识为一个从 Apollo 获取配置的Bean， <code>@RefreshScope</code> 这个很重要，能够自动刷新配置就靠这个了，三个配置缺一不可。
    
    这里还有一个 <code>ConfigRefreshHandler</code> 接口，实现该接口，然后在 accept 上指定要监控的bean配置变更即可，那么就会执行对应Bean配置变更的前后置通知，比如：
    类 <code>ApolloConfigRefreshHandler</code> 如下(需要继承 ConfigRefreshHandler)：
    ```java
    @Component
    public class ApolloConfigRefreshHandler implements ConfigRefreshHandler {
        @Override
        public boolean accept(ConfigChangeEvent changeEvent, String beanName, Object bean) {
            return bean instanceof ApolloConfig;
        }
     
        @Override
        public void beforeRefresh(ConfigChangeEvent changeEvent, String beanName, Object bean) {
            System.err.println("Bean 配置变更了 Before");
        }
    
        @Override
        public void afterRefresh(ConfigChangeEvent changeEvent, String beanName, Object bean) {
            System.err.println("Bean 配置变更了 After");
        }
    }
    ```
    另外有一些使用的抽象类可以使用，如： <code>AbstractTypeConfigRefreshHandler<T></code> 根据类型和beanName来判定是否需要接受监听

## namespace 命名空间
namespace 可以用来区分不同的配置，相当于不同的配置文件，比如在springboot中，我们会定义类似 application-{env}.properties 的文件，会按照每个环境定义， 然后我们启动的时候可以选择激活某些配置文件。

而 namespace 的概念作用类似不同环境中，你同样可以设置多个不同 namespace 的配置，然后在项目启动的时候决定需要激活哪些命名空间。

![alt text](doc/images/namespace-multi.png)

## cluster 集群
【本节内容仅对应用需要对不同集群应用不同配置才需要，如没有相关需求，可以跳过本节】

比如我们有应用在A数据中心和B数据中心（或者说 A 机房和 B 机房）都有部署，那么如果希望两个数据中心的配置不一样的话，我们可以通过新建cluster来解决。

### 新建Cluster
新建Cluster只有项目的管理员才有权限，管理员可以在页面左侧看到“添加集群”按钮。

![alt text](doc/images/create-cluster.png)

点击后就进入到集群添加页面，一般情况下可以按照数据中心来划分集群，如SHAJQ、SHAOY等。

不过也支持自定义集群，比如可以为A机房的某一台机器和B机房的某一台机创建一个集群，使用一套配置。

![alt text](doc/images/create-cluster-detail.png)

### 在Cluster中添加配置并发布
集群添加成功后，就可以为该集群添加配置了，首先需要按照下图所示切换到SHAJQ集群。

![alt text](doc/images/cluster-created.png)

### 指定应用实例所属的Cluster

Apollo会默认使用应用实例所在的数据中心作为cluster，所以如果两者一致的话，不需要额外配置。

如果cluster和数据中心不一致的话，那么就需要通过System Property方式来指定运行时cluster：

- -Dapollo.cluster=SomeCluster
- 这里注意apollo.cluster为全小写
- 在对应激活的 application.properties 中使用 apollo.cluster=SomeCluster




