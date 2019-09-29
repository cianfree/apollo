# apollo-client-starter
主要是为了降低开发的接入门槛，正常来说，我们一个组织，会有一套 apollo 配置中心，并且对应环境的 apollo config service 地址是相对固定的，这样子的话，那么我们系统在部署自己项目的时候，不需要关心具体环境对应的 apollo config service 地址，直接配置对应启动的环境，就可以使用到具体的 apollo 环境的配置。

本质上来说就是将 dev_meta, test_meta, pro_meta .... 之类的配置，上浮到组织提供的jar中进行管理，方便业务接入 apollo 配置中心。


## 发布 jar 
要发布一个 jar， 需要先定义要 deploy 到哪里去，然后再指定 对应的 dev_meta, test_meta, pro_meta ......

deploy 的相关参数：

|**参数**|**说明**|
|:---|:---|
|-Drelease.id=|如果要发布release，要将jar发布到什么仓库的ID标识，通常这个和你本地 <code>settings.xml</code> 中的 <code>servers</code> 对应|
|-Drelease.repo=|如要发布release，要将jar发布到什么私有仓库去，比如 https://nexus.xxx.com/repository/maven-releases/ |
|-Dsnapshot.id=|如果要发布snapshot，要将jar发布到什么仓库的ID标识，通常这个和你本地 <code>settings.xml</code> 中的 <code>servers</code> 对应|
|-Dsnapshot.repo=|如要发布snapshot，要将jar发布到什么私有仓库去，比如 https://nexus.xxx.com/repository/maven-snapshots/ |
|-Ddev_meta=|对应 DEV 环境下的 <code>configservice</code> 服务地址|
|-Dtest_meta=|对应 TEST 环境下的 <code>configservice</code> 服务地址|
|-Dpro_meta=|对应 PRO 环境下的 <code>configservice</code> 服务地址|

可以参考下面这个去修改：

```bash
mvn clean deploy -pl apollo-client-spring-boot-starter -am -Dmaven.test.skip=true -Ddev_meta=http://localhost:8081 -Dtest_meta=http://localhost:8081 -Dpro_meta=http://localhost:8081 -Drelease.id=release -Drelease.repo=https://nexus.xxx.com/repository/maven-releases/ 
```


