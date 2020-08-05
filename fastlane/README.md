fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
## Android
### android clean
```
fastlane android clean
```
Clean
### android pushSandbox
```
fastlane android pushSandbox
```
Push Sandbox to AppCenter
### android pushProdQA
```
fastlane android pushProdQA
```
Push ProdQA to AppCenter
### android pushStaging
```
fastlane android pushStaging
```
Push Staging to AppCenter
### android pushAll
```
fastlane android pushAll
```
Push all

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
