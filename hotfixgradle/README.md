## 热补丁脚本


### v1.0

功能:

- 代码注入，防止类被打上CLASS_ISPEREVERSE标签

- 生成补丁包，修改代码后生成对应的补丁包



分为两个模块：
- release版本，程序的正式发布版本，
- dohot版本，针对版本生成热修复




实现原理：

- 生成release版本
    - 代码注入，在transformClassesWithDexForRelease中注入代码。
    - 保存每一个了类的hash值，以便代码修改时做对比
- 生成热补丁文件，dohot
    - 代码注入，在transformClassesWithDexForDohot中注入代码，
    - 生成每一个类的Hash值，和之前保存的文件进行对比，将hash值不同的文件进行取出并打包


实际操作流程：

- 创建Library Module  Gradle 插件，完成基本的信息配置，并将其发布到本地仓库

-