# Lu Picture Backend

> 一个功能完善的图片管理系统后端服务，支持图片上传、审核、空间管理、用户权限控制等功能。

## 📋 项目简介

Lu Picture Backend 是一个基于 Spring Boot 的图片管理系统后端服务，提供了完整的图片存储、管理、审核和分享功能。系统支持多用户、多空间管理，具备完善的权限控制和图片审核机制。

## ✨ 核心功能

### 用户管理
- 用户注册、登录、注销
- 用户信息管理（头像、昵称、简介）
- 多角色支持：普通用户(user)、管理员(admin)、VIP用户(vip)、超级VIP(svip)
- 基于 Sa-Token 的权限认证

### 图片管理
- 图片上传（支持最大 10MB）
- 图片信息管理（名称、简介、分类、标签）
- 图片元数据自动提取（尺寸、格式、宽高比、主色调）
- 缩略图生成
- 图片审核机制（待审核、通过、拒绝）
- 图片搜索（支持名称、简介、分类、标签）
- 图片生成（基于工作流）

### 空间管理
- 公共空间与私有空间
- 团队空间支持
- 空间级别：普通版、专业版、旗舰版
- 空间配额管理（最大容量、最大图片数量）
- 空间成员管理（查看者、编辑者、管理员）
- 空间数据分析

### 文件存储
- 集成腾讯云 COS 对象存储
- 支持多种图片格式
- 自动生成缩略图

### 其他功能
- WebSocket 实时通信
- Redis 缓存支持
- Caffeine 本地缓存
- 分库分表支持（基于 ShardingSphere）
- 接口文档（Knife4j）
- AOP 日志记录

## 🛠️ 技术栈

### 核心框架
- **Spring Boot** 2.7.6
- **MyBatis Plus** 3.5.9
- **MySQL** 8.x

### 安全与认证
- **Sa-Token** 1.39.0 - 权限认证框架
- **Spring Session** - 分布式会话管理

### 缓存
- **Redis** - 分布式缓存
- **Caffeine** 3.1.8 - 本地缓存

### 数据库
- **ShardingSphere** 5.2.0 - 分库分表
- **MySQL Connector** - 数据库驱动

### 工具库
- **Hutool** 5.8.37 - Java 工具类库
- **Lombok** - 简化 Java 代码
- **Jsoup** 1.15.3 - HTML 解析

### 对象存储
- **腾讯云 COS** 5.6.227

### 文档与监控
- **Knife4j** 4.4.0 - API 文档生成

### 其他
- **WebSocket** - 实时通信
- **Disruptor** 3.4.2 - 高性能无锁队列
- **Spring AOP** - 面向切面编程

## 📁 项目结构

```
lu-picture-backend/
├── src/
│   ├── main/
│   │   ├── java/com/lu/lupicturebackend/
│   │   │   ├── annotation/          # 自定义注解
│   │   │   ├── aop/                 # AOP 切面
│   │   │   ├── api/                 # 外部 API 调用
│   │   │   ├── code/                # 状态码定义
│   │   │   ├── common/              # 通用类
│   │   │   ├── config/              # 配置类
│   │   │   ├── constant/            # 常量定义
│   │   │   ├── controller/          # 控制器层
│   │   │   │   ├── FileController.java
│   │   │   │   ├── PictureController.java
│   │   │   │   ├── SpaceController.java
│   │   │   │   ├── SpaceUserController.java
│   │   │   │   ├── SpaceAnalyzeController.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── MainController.java
│   │   │   ├── exception/           # 异常处理
│   │   │   ├── manager/             # 业务管理层
│   │   │   ├── mapper/              # MyBatis Mapper
│   │   │   ├── model/               # 数据模型
│   │   │   ├── service/             # 服务层
│   │   │   └── utils/               # 工具类
│   │   └── resources/
│   │       ├── application.yaml              # 主配置文件
│   │       ├── application-local.yaml        # 本地环境配置
│   │       ├── mapper/                       # MyBatis XML 映射
│   │       └── static/                       # 静态资源
│   └── test/                        # 测试代码
├── sql/
│   └── create_table.sql             # 数据库建表脚本
├── pom.xml                          # Maven 配置
└── README.md                        # 项目说明文档
```

## 🗄️ 数据库设计

### 核心表结构

#### user - 用户表
- 用户账号、密码、昵称、头像
- 用户角色：user/admin/vip/svip
- 支持逻辑删除

#### picture - 图片表
- 图片 URL、名称、简介、分类、标签
- 图片元数据：尺寸、格式、宽高比、主色调
- 审核状态：待审核/通过/拒绝
- 关联用户和空间
- 支持缩略图

#### space - 空间表
- 空间名称、级别（普通/专业/旗舰）
- 空间类型：私有/团队
- 配额管理：最大容量、最大图片数
- 使用统计：当前容量、当前图片数

#### space_user - 空间成员表
- 空间与用户关联
- 成员角色：viewer/editor/admin
- 唯一索引保证用户在空间中角色唯一

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/luyixun2004/lu-picture-backend.git
cd lu-picture-backend
```

2. **创建数据库**
```bash
# 执行数据库脚本
mysql -u root -p < sql/create_table.sql
```

3. **配置文件**

编辑 `src/main/resources/application-local.yaml`，配置以下信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lu_picture
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: your_redis_password

# 阿里云 AI 配置（如需使用）
aliyunAi:
  apikey: your_api_key
```

4. **安装依赖**
```bash
mvn clean install
```

5. **启动项目**
```bash
mvn spring-boot:run
```

6. **访问接口文档**

启动成功后访问：`http://localhost:8123/api/doc.html`

## 📡 API 接口

### 基础路径
```
http://localhost:8123/api
```

### 主要接口模块

#### 用户接口 (`/user`)
- `POST /register` - 用户注册
- `POST /login` - 用户登录
- `POST /logout` - 用户登出
- `GET /get/login` - 获取当前登录用户
- `POST /update` - 更新用户信息

#### 图片接口 (`/picture`)
- `POST /upload` - 上传图片
- `POST /add` - 添加图片信息
- `POST /delete` - 删除图片
- `POST /update` - 更新图片信息
- `GET /get/vo` - 获取图片详情
- `POST /list/page/vo` - 分页查询图片
- `POST /review` - 审核图片

#### 空间接口 (`/space`)
- `POST /add` - 创建空间
- `POST /delete` - 删除空间
- `POST /update` - 更新空间信息
- `GET /get/vo` - 获取空间详情
- `POST /list/page/vo` - 分页查询空间

#### 空间成员接口 (`/space/user`)
- `POST /add` - 添加空间成员
- `POST /delete` - 移除空间成员
- `POST /update` - 更新成员角色
- `POST /list/page/vo` - 查询空间成员

#### 文件接口 (`/file`)
- `POST /upload` - 文件上传
- `POST /upload/batch` - 批量上传

#### 空间分析接口 (`/space/analyze`)
- `GET /rank` - 空间使用排行
- `GET /category` - 分类统计
- `GET /tag` - 标签统计

## ⚙️ 配置说明

### 应用配置
```yaml
server:
  port: 8123                    # 服务端口
  servlet:
    context-path: /api          # 上下文路径
```

### 数据库配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/lu_picture
    username: root
    password: 123456
```

### Redis 配置
```yaml
spring:
  redis:
    database: 1
    host: localhost
    port: 6379
    password: root@123456
    timeout: 5000
```

### 文件上传配置
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB       # 最大文件大小
```

### 分表配置
```yaml
spring:
  shardingsphere:
    rules:
      sharding:
        tables:
          picture:
            actual-data-nodes: lu_picture.picture
            table-strategy:
              standard:
                sharding-column: spaceId
```

## 🔐 权限说明

### 角色权限
- **user** - 普通用户：可以上传图片、创建私有空间
- **admin** - 管理员：可以审核图片、管理所有空间
- **vip** - VIP用户：更大的空间配额
- **svip** - 超级VIP：最大的空间配额和功能权限

### 空间角色
- **viewer** - 查看者：只能查看空间内容
- **editor** - 编辑者：可以上传、编辑图片
- **admin** - 管理员：可以管理空间成员和设置

## 📝 开发说明

### 代码规范
- 使用 Lombok 简化代码
- 统一异常处理
- RESTful API 设计
- 使用 MyBatis Plus 简化数据库操作

### 日志配置
- 使用 SLF4J + Logback
- SQL 日志输出（开发环境）
- AOP 记录关键操作日志

### 缓存策略
- Redis 存储会话信息
- Caffeine 本地缓存热点数据
- 多级缓存提升性能

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 👨‍💻 作者

**luyixun2004**

- GitHub: [@luyixun2004](https://github.com/luyixun2004)

## 🙏 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis Plus](https://baomidou.com/)
- [Sa-Token](https://sa-token.cc/)
- [Hutool](https://hutool.cn/)
- [Knife4j](https://doc.xiaominfo.com/)

---

如有问题或建议，欢迎提交 Issue！
