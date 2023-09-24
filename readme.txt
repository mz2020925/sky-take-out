对工程的每个模块作用说明：
| 1    | sky-take-out | maven父工程，统一管理依赖版本，聚合其他子模块                |
| 2    | sky-common   | 子模块，存放公共类，可以供其他模块使用，例如：工具类、常量类、异常类等           |
| 3    | sky-pojo     | 子模块，存放实体类(entity)、VO、DTO等，entity、VO、DTO是对POJO的细分        |
| 4    | sky-server   | 子模块，后端服务，配置文件、配置类、拦截器、controller、service、mapper、启动类等 |

分析sky-common模块的每个包的作用：
| constant    | 存放相关常量类                 |
| context     | 存放上下文类                   |
| enumeration | 项目的枚举类存储               |
| exception   | 存放自定义异常类               |
| json        | 处理json转换的类               |
| properties  | 存放SpringBoot相关的配置属性类 |
| result      | 返回结果类的封装               |
| utils       | 常用工具类                     |

分析sky-pojo模块的每个包的作用：
| Entity | 实体，通常和数据库中的表对应                 |
| DTO    | 数据传输对象，通常用于程序中各层之间传递数据 |
| VO     | 视图对象，为前端展示数据提供的对象           |
| POJO   | 普通Java对象，只有属性和对应的getter和setter |

分析sky-server模块的每个包的作用：
| config         | 存放配置类       |
| controller     | 存放controller类 |
| interceptor    | 存放拦截器类     |
| mapper         | 存放mapper接口   |
| service        | 存放service类    |
| SkyApplication | 启动类           |