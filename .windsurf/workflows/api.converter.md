---
description: 将 Spring MVC 控制器接口转换为 Vue 前端 API 接口，包含上下文补全、命名规范和代码质量保障。
auto_execution_mode: 1
---

## 用户输入

```text
$ARGUMENTS
```

如果用户未提供 Controller 文件路径（即命令为空），必须提示用户输入 Controller 文件路径后再开始执行。

## 大纲

整个流程分为四个阶段。
**不得自动跨阶段执行。**
每一阶段结束前，若提示“等待用户输入”，模型必须暂停并等待用户响应。

## 阶段

### 阶段 0：上下文收集与补全

**任务目标**：
- 读取并分析 Controller 文件；
- 提取基础路径、功能模块线索、接口方法、参数与返回类型；
- 识别所有相关的 DTO/VO/Page 类；
- 如遇未知类或无法解析的引用，**必须暂停并询问用户提供该类定义**。

**执行规则**：
- 不得假设类结构；
- 对每个未找到的类输出提示：“无法找到类 XxxDTO 的定义，请提供其字段结构或源文件路径。”
- 在完成所有上下文补全后，进入阶段 1。

### 阶段 1：命名决策与目录确认（交互阶段）

**目标**：
确定生成的 Vue API 文件所属模块及文件名。

**强制交互规则**：
1. **功能模块确认**：
   - 你必须输出以下提示，并等待用户输入：
      ```markdown
      ❓ 请输入该 API 文件所属功能模块（例如：system、logistics）
      在用户确认前不得继续执行。
      ``` 
   - 用户输入后，将模块路径设置为： `startimes-app-ui/src/api/{功能模块}/` 。
   - 若不存在，后续阶段会创建。
2. **API 文件命名**：
    - 自动生成建议名称（如：portApi.js）。
    - 你必须输出以下提示，并等待用户输入：
        ```markdown
        💡 建议的 API 文件名为 {建议名}
        是否接受此名称？（输入 y 接受，或输入新名称）
        ``` 
    - 用户确认后，进入下一阶段。

3. **现有文件检查**：
   - 检查目标目录中是否已存在相关 API 文件
   - 检查 index.js 是否存在
   - 如需要合并到现有文件，读取该文件内容

### 阶段 2：API 转换执行

1. **生成 API 文件**：
   - 创建符合规范的 API JS 文件
   - 使用 ES6 模块语法
   - 导入必要的工具（`@/utils/request`）

2. **API 方法实现**：
   - 为每个后端接口生成对应的前端方法
   - 正确映射 HTTP 方法（GET/POST/PUT/DELETE）
   - 处理路径参数（如 `/{id}`）
   - 正确映射请求参数（@RequestBody → data, @RequestParam → params, @PathVariable → url）
   - 为分页查询方法添加 PageCondition 参数映射

3. **类型映射**：
   - PageQueryResult<T> → { total: number; records: T[] }
   - PageCondition → { page: number; limit: number } + 查询条件
   - AbstractDTO 继承的属性需包含在前端类型定义中

4. 为每个接口生成 JSDoc 注释，包含方法描述、参数说明和返回值说明。

示例生成格式：
```js
/**
 * Get user list by query parameters
 * @param {Object} params - Query parameters
 * @param {number} params.limit - Page size
 * @param {number} params.page - Page number
 * @param {number} params.companyId - Company ID
 * @returns {Promise<{
 *  data: Array<{
 *    id: number,
 *    companyId: number,
 *    name: string,
 *    mem: string,
 *    version: number,
 *    createId: number,
 *    createCode: string,
 *    createInstant: string,
 *    modifyId: number,
 *    modifyCode: string,
 *    modifyInstant: string,
 *    transactionId: string,
 *    serverName: string,S
 *    createName: string,
 *    modifyName: string,
 *    companyName: string
 *  }>,
 *  pages: number,
 *  total: number
 * }>} 
 */
export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}
```

### 阶段 3：质量控制与验证

1. **代码规范验证**：
   - 检查是否包含 JSDoc 注释
   - 验证 API 路径完整性
   - 确认参数映射正确性
   - 检查错误处理机制

2. **文件导出验证**：
   - 确认新 API 文件已在 index.js 中正确导出
   - 验证导出语法为 `export * from './filename'`

3. **命名一致性**：
   - 验证常量命名（如 `userManagementApi`）
   - 检查方法命名（如 `getUserList`, `createUser`）

**输出**：符合规范的 API 文件和更新的 index.js

## 关键规则

- **必需的上下文**：在转换前必须获取所有相关类的完整定义，特别是基础类
- **命名约定**：功能模块和文件名必须由用户确认或基于明确规则确定
- **API 规范**：
  - 使用 ES6 模块语法
  - 包含 JSDoc 注释
  - 正确处理分页类型
  - 遵循错误处理约定
  - 遵循 axios 请求模式
- **文件位置**：API 文件必须位于 `docs/api/{功能模块}/` 目录下
- **导出规范**：必须在对应目录的 index.js 中使用 `export * from` 进行导出

## 处理策略

### 对于缺失的类定义：
1. 首先尝试在代码库中搜索
2. 如果找不到，要求用户提供类的字段结构
3. 基于提供的信息生成对应的 TypeScript 类型定义

### 对于功能模块和文件命名不确定：
1. 询问用户关于功能模块分类
2. 根据 Controller 的包结构或功能自动建议模块名
3. 确认文件命名以避免冲突

### 对于 API 文件规范问题：
1. 强制使用预定义的模板结构
2. 验证每个生成的 API 方法是否符合标准格式
3. 在生成后进行格式和内容验证