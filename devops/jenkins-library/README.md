Jenkins Shared Library（企业版）使用说明

目录结构
- vars/：对外暴露的全局步骤（如 ciPipeline、dockerBuild、helmDeploy）
- src/org/company/ci/：内部工具类（Shell/Utils）
- resources/ci/scripts/：脚本（如 docker_login.sh）
- resources/ci/templates/：模板（如 helm-values-demo.yaml）
- docs/：规范与风格（可选）
- test/：JenkinsPipelineUnit 测试样例（可选）

Jenkins 全局配置
- Manage Jenkins → System → Global Pipeline Libraries：
  - Name：company-ci（示例）
  - Default version：main 或发布分支
  - Retrieval method：Modern SCM（推荐）
  - Project repository：指向该库的 Git 仓库
  - Load implicitly：按需（建议关闭，使用 @Library 显式声明）

Jenkinsfile 使用示例
- 在目标项目 Jenkinsfile 顶部增加：
  @Library('company-ci@v1') _
- 调用标准流水线：
  ciPipeline(
    language: 'go',
    projectDir: 'apps/svc-go',
    docker: [ registry: 'ghcr.io/org/repo', image: 'svc-go', push: true ],
    helm:   [ chart: 'charts/svc-go', namespace: 'dev', release: 'svc-go', timeout: '5m' ],
    gates:  [ coverage: 80, lint: true, security: true ]
  )

最佳实践
- 参数显式化且有默认值；敏感信息通过 Credentials Binding 注入
- Shell 统一使用 src/org/company/ci/Shell 封装（set -euo pipefail + 重试）
- 质量门禁失败即阻断；部署需具备回滚路径与审批门禁（prod）
- 支持 Monorepo：路径过滤 + 受影响模块构建

测试（可选）
- 使用 JenkinsPipelineUnit 在本地/CI 中运行单测
- 参考 test/ 下示例