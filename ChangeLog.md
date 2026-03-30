#### 2026-03-30 移除 module_datastore

> 项目实际使用 Proto DataStore（Wire 生成），Preferences DataStore 封装模块无调用方，予以删除。
> 原模块包含：DataStoreOwner / IDataStoreOwner / DataStorePreference / PreferenceProperty / DataStoreInitProvider。
> 如需恢复，参考 git 历史或 androidx.datastore:datastore-preferences。

#### 2022-03-18 重大更新

> 修改java annotationProcessorOptions to ksp arg
>
> 0aa637e71680df76d69352c908424964c017606

#### 2022-05-20 统一依赖管理

> 统一依赖管理
>
> 96fd607ff1a77f0576aa89fa7eb39c2ddbcccf23

#### 2023-02-06 移除image compress

> 移除image compress
>
> 40cb906438567c901b7ecc334f2283d006a70f93

#### 2023-02-08 统一依赖管理

> 统一依赖管理
>
> 15c34e493d699a524a0e3dff71041999df577dce
