# pocDemo

![Logo](https://s.beta.gtimg.com/rdmimg/exp/image2/2018/06/08/_27617a9f-5695-4cd8-ac5a-a05fe10f7525.png)

一个可使用极简API实现以下功能的SDK

  - 音频通话
  - 视频通话
  - 即时消息
  - 集群对讲
  - 视频监控
  - 位置上报
  - 一键报警

## Download

Kalle uses URLConnection handle socket by default, add this dependency using Gradle:

```groovy
implementation 'com.yanzhenjie:kalle:0.1.7'
```

If you want to use okhttp handle socket, add this dependency using Gradle:

```groovy
implementation 'com.yanzhenjie:okalle:0.1.7'
```

Kalle requires at minimum Android 2.3(Api level 9).

## Contributing

Before submitting pull requests, contributors must abide by the [agreement](CONTRIBUTING.md) .

## License

```text
Copyright 2019 Zhenjie Yan

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
