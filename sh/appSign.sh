#!/usr/bin/env bash

ksPath=../yktest.jks
ksAlias=yktest
ksPass=qaz123

# 待签名App
originPath=$1

originPathLength=$((${#originPath} - 4))

# app签名
java -jar apksigner.jar sign \
  --v3-signing-enabled false \
  --v4-signing-enabled false \
  --ks $ksPath \
  --ks-key-alias $ksAlias \
  --ks-pass pass:$ksPass \
  --key-pass pass:$ksPass \
  --out "${originPath:0:originPathLength}"_sign.apk \
  "$originPath"

# 调用外部check脚本检测签名状态
exec ./signCheck.sh "${originPath:0:originPathLength}"_sign.apk
