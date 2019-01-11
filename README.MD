自定义实现RPC框架的思路：动态代理+通讯+反射
1、动态代理：service->serviceImpl
2、通讯：netty
> spring加载配置文件，维护service和serviceImpl之间的对应关系（暴露服务），通过jdk动态代理方式，在请求端调用service接口时，获取到请求接口名、接口方法名、请求参数类型、请求参数信息，封装为request对象，然后以json形式发送到实现端netty，实现端netty接收request后，获取到请求端请求接口信息，通过反射调用获取对应serviceImpl类中方法，并将返回结果发送到请求端netty，然后作为远程过程调用的返回值。

