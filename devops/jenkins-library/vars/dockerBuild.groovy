def call(Map args = [:]) {
  def shx = new org.company.ci.Shell(this)

  def registry = args.get('registry', '')
  def image    = args.get('image', '')
  def context  = args.get('context', '.')
  def file     = args.get('file', 'Dockerfile')
  def push     = args.get('push', true)
  def extraTags = (args.get('extraTags', []) as List)
  def buildArgs = (args.get('buildArgs', [:]) as Map)
  def platform = args.get('platform', 'linux/amd64')

  if (!registry || !image) {
    error "dockerBuild: registry/image required"
  }

  def fullTag = "${registry}/${image}:${env.GIT_COMMIT?.take(7) ?: env.BUILD_NUMBER}"
  def tagsArgs = ([fullTag] + extraTags.collect { "${registry}/${image}:${it}" })
                  .unique()
                  .collect { "-t ${it}" }
                  .join(' ')

  def buildArgFlags = buildArgs.collect { k, v -> "--build-arg ${k}=${v}" }.join(' ')

  shx.run("docker version || true")
  withEnv(["DOCKER_BUILDKIT=1"]) {
    shx.run("docker build ${tagsArgs} -f ${file} ${buildArgFlags} --platform ${platform} ${context}")
    if (push) {
      withCredentials([usernamePassword(credentialsId: args.get('credentialsId', ''), usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
        shx.run("echo \"${env.REG_PASS}\" | docker login ${registry} -u \"${env.REG_USER}\" --password-stdin", 1, 3)
      }
      ([fullTag] + extraTags.collect { "${registry}/${image}:${it}" }).unique().each { t ->
        shx.run("docker push ${t}")
      }
    }
  }
}