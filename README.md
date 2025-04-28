# Common Library for Spring Boot Applications

A lightweight library that provides useful features for Spring Boot applications.

## Installation

### Maven

Add the repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/swaswa-core/microservice</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.joshuasalcedo</groupId>
        <artifactId>common-library</artifactId>
        <version>0.0.1</version>
    </dependency>
</dependencies>
```

### GitHub Packages Authentication

To download packages from GitHub Packages, you'll need to authenticate with a GitHub token.

Add the following to your `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

Replace `YOUR_GITHUB_USERNAME` with your GitHub username and `YOUR_GITHUB_TOKEN` with a GitHub personal access token that has the `read:packages` scope.

## Features

---

### 1. Banner Feature

A customizable banner implementation for Spring Boot applications.

#### Example Banner
```
 ███████╗    ██╗    ██╗     █████╗     ███████╗    ██╗    ██╗     █████╗
██╔════╝    ██║    ██║    ██╔══██╗    ██╔════╝    ██║    ██║    ██╔══██╗
███████╗    ██║ █╗ ██║    ███████║    ███████╗    ██║ █╗ ██║    ███████║
╚════██║    ██║███╗██║    ██╔══██║    ╚════██║    ██║███╗██║    ██╔══██║
███████║    ╚███╔███╔╝    ██║  ██║    ███████║    ╚███╔███╔╝    ██║  ██║
╚══════╝     ╚══╝╚══╝     ╚═╝  ╚═╝    ╚══════╝     ╚══╝╚══╝     ╚═╝  ╚═╝

Application Name: ${spring.application.name}
Application Version: ${spring.application.version}
${AnsiColor.GREEN} :: Spring Boot${spring-boot.formatted-version} :: ${AnsiColor.DEFAULT}
```

#### Usage

The banner will automatically activate when added to your Spring Boot application's dependencies. No additional configuration is required for basic usage.

#### Configuration Properties

Configure the banner in your `application.properties` or `application.yml`:

```properties
# Enable/disable the custom banner (default: true)
io.joshuasalcedo.banner.enabled=true

# Banner text color (default: default)
# Options: default, black, red, green, yellow, blue, magenta, cyan, white, 
# bright_black, bright_red, bright_green, bright_yellow, bright_blue, 
# bright_magenta, bright_cyan, bright_white
io.joshuasalcedo.banner.color=green

# Custom banner location (default: classpath:banner.txt)
io.joshuasalcedo.banner.location=classpath:my-custom-banner.txt

# Set application name and version for banner display
spring.application.name=my-application
spring.application.version=@project.version@
```

For the version to be properly displayed, enable resource filtering in your Maven `pom.xml`:

```xml
<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
        </resource>
    </resources>
</build>
```
---


### 2. [Feature Name]

[Brief description of what this feature does]

#### Usage

[Example code showing how to use this feature]

```java
// Example code here
```

#### Configuration Properties

Configure in your `application.properties` or `application.yml`:

```properties
# Enable/disable feature (default: true)
io.joshuasalcedo.[feature].enabled=true

# Other configuration options
io.joshuasalcedo.[feature].property1=value1
io.joshuasalcedo.[feature].property2=value2
```

#### Additional Information

[Any other information, examples, or notes about this feature]