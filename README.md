# 归档通知 #20210516
这个项目是包含了 Swing KotlinDSL和shell command line executor 现在有了更好的解决方案
* kotlin swing dsl -> jetpack compose (or jetbrains compose)
* commandline executor -> [khell](https://github.com/ElisaMin/Khell)

这个项目也被重写了 欢迎预览新的项目：[HFT](https://github.com/ElisaMin/Heizi-Flashing-Tools)

# 恭喜你发现史前巨坟
（2020.4.10 写下）
```
# Hello World
## 又一个死库
```
# 分支
**master** 初次完成。    
**Awt**    已完成
# 文件目录 
```
. me.heizi
  .swing
    .lib.kt 其实里面和swing无关。
    .swingx.kt Swing的DSL扩展
  .tools
    .HeiziTools.kt 主角
  .utills
    .CommandExecutor.kt subprocess的封装
    .CommandResult.kt 得到结果
    .PlatformTools.kt subprocess的封装的封装
```
