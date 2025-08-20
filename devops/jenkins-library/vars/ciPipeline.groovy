def call(Map cfg = [:]) {
  cfg = [
    language  : cfg.get('language', 'go'),              // go/node/python/java
    projectDir: cfg.get('projectDir', '.'),             // 子目录/项目根
    docker    : cfg.get('docker', [:]),                 // registry/image/context/file/push/extraTags/buildArgs
    helm      : cfg.get('helm', [:]),                   // release/chart/namespace/timeout/valuesFile
    gates     : cfg.get('gates', [coverage: 0, lint: true, security: false])
  ]

  def shx = new org.company.ci.Shell(this)
  def utils = new org.company.ci.Utils(this)

  node {
    try {
      stage('Prepare') {
        checkout scm
        shx.run('git --version')
        if (cfg.language == 'node') {
          shx.run('corepack enable || true')
        }
      }

      stage('Deps') {
        dir(cfg.projectDir) {
          if (cfg.language == 'go') {
            shx.run('go mod download')
          } else if (cfg.language == 'node') {
            shx.run('pnpm i --frozen-lockfile || npm ci')
          } else if (cfg.language == 'python') {
            shx.run('python -V && pip install -r requirements.txt')
          } else if (cfg.language == 'java') {
            shx.run('./mvnw -v || mvn -v')
          }
        }
      }

      stage('Static') {
        if (cfg.gates.lint) {
          dir(cfg.projectDir) {
            if (cfg.language == 'go') {
              shx.run('go vet ./...')
            } else if (cfg.language == 'node') {
              shx.run('npm run lint || pnpm run lint || true')
            } else if (cfg.language == 'python') {
              shx.run('flake8 || ruff check . || true')
            } else if (cfg.language == 'java') {
              shx.run('mvn -q -DskipTests -Dspotbugs.skip=false verify || true')
            }
          }
        }
      }

      stage('Test') {
        dir(cfg.projectDir) {
          if (cfg.language == 'go') {
            sh "go test -race -coverprofile=coverage.out ./..."
            utils.archiveIfExists('coverage.out')
            utils.enforceCoverage('coverage.out', cfg.gates.coverage as int)
          } else if (cfg.language == 'node') {
            shx.run('npm test -- --ci --coverage || pnpm test -- --ci --coverage')
            utils.archiveIfExists('coverage')
          } else if (cfg.language == 'python') {
            shx.run('pytest -q --maxfail=1 --disable-warnings --cov=. --cov-report=xml')
            utils.archiveIfExists('coverage.xml')
          } else if (cfg.language == 'java') {
            shx.run('mvn -q -DskipITs=false test')
          }
        }
      }

      stage('Security') {
        if (cfg.gates.security) {
          dir(cfg.projectDir) {
            shx.run('echo "Run dependency and container scans here (trivy/grype/semgrep)..."')
          }
        }
      }

      stage('Build & Publish Image') {
        if ((cfg.docker.push ?: false) && cfg.docker.image) {
          dockerBuild(cfg.docker + [projectDir: cfg.projectDir])
        } else {
          echo "Skipping docker build: push=${cfg.docker.push}, image=${cfg.docker.image}"
        }
      }

      stage('Deploy') {
        if (cfg.helm?.chart && cfg.helm?.release) {
          helmDeploy(cfg.helm)
        } else {
          echo "Skipping deploy: helm.release/chart not configured"
        }
      }
    } catch (err) {
      currentBuild.result = 'FAILURE'
      throw err
    } finally {
      stage('Post') {
        echo "Build result: ${currentBuild.result}"
      }
    }
  }
}