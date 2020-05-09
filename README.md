# pocDemo

![Logo](https://s.beta.gtimg.com/rdmimg/exp/image2/2018/06/08/_27617a9f-5695-4cd8-ac5a-a05fe10f7525.png)

一个可使用pocSDK实现以下功能的Demo

  - 音频通话
  - 视频通话
  - 即时消息
  - 集群对讲
  - 视频监控
  - 位置上报
  - 一键报警

## 项目中引入SDK

pocSDK 发布在Maven中，使用Gradle构建:

```groovy
//根目录下build.gradle添加
allprojects {
    repositories {
        jcenter()
        maven { url 'https://dl.bintray.com/benzly/maven' }
    }
}

//app下build.gradle添加
dependencies {
    implementation 'com.huamai:poc:2.0.7'
}
```

pocSDK requires at minimum Android 4.4(Api level 19).

## SDK文档

SDK详细使用说明[wiki](https://github.com/benzly/pocDemo/wiki)

## License

```text
Copyright 2020 HuaiMai

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
