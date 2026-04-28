## Shared-library

#  Jenkins Shared Library CI/CD Pipeline (Docker Project)

## Overview

This project demonstrates how to use a **Jenkins Shared Library** to build a reusable CI/CD pipeline for a Docker-based application.

Instead of writing pipelines in every project, we centralize logic using a shared library.

---

## Objective

* Reuse pipeline code across projects
* Build Docker image from source code
* Push image to DockerHub
* Understand Jenkins Shared Library structure

---

##  Architecture

```
GitHub Repo (App Code)
        ↓
Jenkins Pipeline
        ↓
Shared Library (Reusable Code)
        ↓
Docker Build & Push
```

---

##  Repository Structure

### 🔹 Shared Library Repo

```
jenkins-shared-library/
│
├── vars/
│   ├── ciPipeline.groovy
│   ├── dockerBuild.groovy
│   ├── dockerPush.groovy
│   └── gitCheckout.groovy
│
└── src/
    └── com/devops/utils.groovy
```

---

##  Jenkins Setup

### 1️⃣ Install Git on Jenkins Server

```bash
sudo yum install git -y  
```

---

### 2️⃣ Configure Shared Library

Go to:

**Manage Jenkins → Configure System → Global Pipeline Libraries**

Add:

* Name: `mind-lib`
* Default Version: `main`
* SCM: Git
* Repository URL:

  ```
  https://github.com/jadalaramani/jenkins-shared-library.git
  ```

---

### 3️⃣ Add DockerHub Credentials

Go to:

**Manage Jenkins → Credentials**

Add:

* Kind: **Username with Password**
* ID: `dockerhub-cred`
* Username: your DockerHub username
* Password: your DockerHub password

---

##  Shared Library Code

### 🔹 gitCheckout.groovy

```groovy
def call(String repoUrl, String branch='main') {
    git branch: branch, url: repoUrl
}
```

---

### 🔹 dockerBuild.groovy

```groovy
def call(String imageName, String tag) {
    sh "docker build -t ${imageName}:${tag} ."
}
```

---

### 🔹 dockerPush.groovy

```groovy
def call(String imageName, String tag, String credId) {

    withCredentials([usernamePassword(
        credentialsId: credId,
        usernameVariable: 'DOCKER_USER',
        passwordVariable: 'DOCKER_PASS'
    )]) {

        sh """
        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
        docker push ${imageName}:${tag}
        """
    }
}
```

---

### 🔹 ciPipeline.groovy

```groovy
def call(Map config) {

    def image = config.imageName

    pipeline {
        agent any

        environment {
            TAG = "${BUILD_NUMBER}"
        }

        stages {

            stage('Checkout') {
                steps {
                    gitCheckout(config.repoUrl)
                }
            }

            stage('Build Docker Image') {
                steps {
                    sh "docker build -t ${image}:${TAG} ."
                }
            }

            stage('Push Image') {
                steps {
                    dockerPush(image, TAG, config.dockerCred)
                }
            }

            stage('Deploy') {
                steps {
                    echo "Deploying ${image}:${TAG}"
                }
            }
        }
    }
}
```

---

##  Jenkinsfile (Application Repo)

```groovy
@Library('mind-lib') _

ciPipeline(
    repoUrl: 'https://github.com/jadalaramani/jenkins-shared-library.git',
    imageName: 'ramanijadalla/mindcircuit17d',
    dockerCred: 'dockerhub-cred'
)
```

---

## ▶️ Pipeline Execution Flow

1. Jenkins pulls source code
2. Builds Docker image
3. Tags image with build number
4. Logs into DockerHub
5. Pushes image
6. Deploy stage runs

---

##  Common Errors & Fixes

### ❌ Git not found

Install Git:

```bash
sudo yum install git -y
```

---

### ❌ Credential type mismatch

Use:

```
usernamePassword (NOT string)
```

---

### ❌ Environment variable error

Avoid:

```groovy
IMAGE_NAME = config.imageName
```

Use:

```groovy
def image = config.imageName
```

---

##  Key Learnings

* Jenkins Shared Library structure
* Reusable pipeline design
* Secure credential handling
* Docker CI/CD workflow

---


## 👨‍💻 Author

Jadala Ramani
DevOps Engineer

---
