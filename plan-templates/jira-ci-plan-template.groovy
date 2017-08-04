plan(key:'JCCTF',name:'Capture - JIRA Cloud (Fork)', description:'Run tests on JIRA Cloud') {

    project(key:'JCAP',name:'JIRA Capture')
    repository(name:'jira-capture')

    trigger(type:'polling',strategy:'periodically',frequency:'180') {
        repository(name:'jira-capture')
    }

    notification(type:'All Builds Completed',recipient:'hipchat',
            apiKey:'${bamboo.atlassian.hipchat.apikey.password}',
            notify:'false',room:'433794')

    notification(type:'Failed Builds and First Successful',
            recipient:'committers')

    stage(name:'Default Stage') {
        job(key:'JCCIT',name:'JIRA Cloud Integration tests') {
            requirement(key:'system.hg.executable',condition:'exists')
            requirement(key:'os',condition:'equals',value:'Linux')
            miscellaneousConfiguration(cleanupWorkdirAfterBuild:'true')

            task(type:'checkout',description:'Checkout Default Repository',
                    cleanCheckout:'true') {
                repository(name:'jira-capture')
            }
            task(type:'script',description:'VNC server setup',
                    script:'plugin-func-tests/vnc-setup.sh',environmentVariables:'DISPLAY=":20"',
                    interpreter:'LEGACY_SH_BAT')

            task(type:'maven3',description:'Run Functional Tests (selenium Maven)',
                    goal:'-B clean verify -P jira-cloud-profile -Djira.security.disabled=true -Djava.awt.headless=true -Djira.minify.skip=true -DtestGroups=bonfire-jira-integration -Dci.build.number=${bamboo.buildNumber}  -pl plugin-func-tests -am',
                    mavenExecutable:'Maven 3.2',buildJdk:'JDK 1.8',
                    environmentVariables:'MAVEN_OPTS="-Xmx1024m" -XX:MaxPermSize=256m" DISPLAY=":20"',
                    hasTests:'true',testDirectory:'**/surefire-reports/*xml, **/test-reports/*xml,**/group-bonfire-jira44/**/surefire-reports/*.xml')
        }
        job(key:'JCCRT',name:'JIRA Cloud Rest tests') {
            requirement(key:'os',condition:'equals',value:'Linux')
            miscellaneousConfiguration(cleanupWorkdirAfterBuild:'true')

            task(type:'checkout',description:'Checkout Default Repository',
                    cleanCheckout:'true') {
                repository(name:'jira-capture')
            }

            task(type:'maven3',description:'Builds the JIRA Capture plugin',
                    goal:'-B clean verify -DtestGroups=bonfire-jira-rest -P jira-cloud-profile -Dci.build.number=${bamboo.buildNumber}  -pl plugin-func-tests -am',
                    mavenExecutable:'Maven 3.2',buildJdk:'JDK 1.8',
                    environmentVariables:'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"',
                    hasTests:'true',testDirectory:'**/surefire-reports/*xml, **/test-reports/*xml,**/group-bonfire-jira44/**/surefire-reports/*.xml')
        }
    }
    branchMonitoring() {
        createBranch(matchingPattern:'.*')
        inactiveBranchCleanup(periodInDays:'30')
        deletedBranchCleanup(periodInDays:'7')
    }
    permissions() {
        anonymous(permissions: 'read')
        loggedInUser(permissions: 'read,write,build,clone,administration')
    }
}