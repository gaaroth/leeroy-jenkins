def hipChatRoom = 'LeeroyJenkins'

node('slave') {
    try {
        
        // Mark the code checkout 'stage'....
        stage 'Checkout'
        // Get some code from the repo
		checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/gaaroth/leroy-jenkins']]])
        
        
        // Get the maven tool.
        // ** NOTE: This 'mvn 3.3.9' maven tool must be configured
        // **       in the global configuration.
        def mvnHome = tool 'mvn 3.3.9'
        wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1425975759468', targetLocation: 'settings.xml', variable: '']]]) {
    
            // Mark the code build 'stage'....
            stage 'Build'
            try {
            //def v = version()
            // Set version to something final
            //sh "${mvnHome}/bin/mvn -s settings.xml versions:set -DnewVersion=${v.full}-${env.BUILD_NUMBER}"
            // Run the maven build, we skip tests and checkstyle since we run them later
            sh "${mvnHome}/bin/mvn -s settings.xml -DskipTests clean package install:install"
            } catch (all) {
                error 'Build'
            }

            // deploy is long running, so its run in parallel with tests and checkstyle
            stage 'Test / QA'
			try {
				// run tests and archive results
				sh "${mvnHome}/bin/mvn -s settings.xml -Dmaven.test.failure.ignore surefire:test"
				step([$class: 'JUnitResultArchiver', allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'])
				// perform static analysis and archive results
				sh "${mvnHome}/bin/mvn -s settings.xml site"
				step([$class: 'CheckStylePublisher', canComputeNew: false, defaultEncoding: '', healthy: '10', pattern: '', unHealthy: '200'])
				step([$class: 'PmdPublisher', canComputeNew: false, defaultEncoding: '', healthy: '0', pattern: '', thresholdLimit: 'normal', unHealthy: '100'])
				step([$class: 'FindBugsPublisher', canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '0', includePattern: '', pattern: '**/findbugsXml.xml', thresholdLimit: 'normal', unHealthy: '30'])
			} catch (all) {
				error 'Test'
			}
			
            stage 'Deploy'
			try {
				// start server and deploy updated application. We use a file stored here in Jenkins to handle integration server configuration
				wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: '68f4c947-5db7-43d2-b819-57a141264d3d', targetLocation: 'leeroy-server.xml', variable: '']]]) {
					sh "${mvnHome}/bin/mvn -s settings.xml liberty:clean-server liberty:stop-server liberty:start-server -DconfigFile=leeroy-server.xml -Dtimeout=120"    
				}
			} catch (all) {
				error 'Deploy'
			}
            
            //stage 'Integration test'
            //try {
            //    sh "${mvnHome}/bin/mvn -s settings.xml -Dmaven.test.failure.ignore failsafe:integration-test"
            //    step([$class: 'JUnitResultArchiver', allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml'])
            //} catch (all) {
            //    error 'Integration test'
            //}
            
            //stage 'Archive'
            //try {
            //    archive '**/target/*.war'
            //} catch (all) {
            //    error 'Archive'
            //}
            
            def msg = "${env.JOB_NAME} #${env.BUILD_NUMBER} Build ${currentBuild.result} (<a href=\"${env.BUILD_URL}\">Open</a>)"
            if (currentBuild.result == "UNSTABLE") {
                hipchatSend color: 'YELLOW', message: msg, room: hipChatRoom, v2enabled: false, notify: true
            }
            if (currentBuild.result == "ABORTED") {
                hipchatSend color: 'GRAY', message: msg, room: hipChatRoom, v2enabled: false
            }
            if (currentBuild.result == "STABLE") {
                hipchatSend color: 'GREEN', message: msg, room: hipChatRoom, v2enabled: false, notify: true
            }
        }
    } catch (all) {
        hipchatSend color: 'RED', message: "${env.JOB_NAME} #${env.BUILD_NUMBER} Build failed on stage ${all} (<a href=\"${env.BUILD_URL}\">Open</a>)", room: hipChatRoom, v2enabled: false, notify: true
        throw all
    }
}


//stage 'Production Deploy'
//timeout(5) {
//    def deployToProd = input message: 'Deploy in ambiente di produzione?', parameters: [booleanParam(defaultValue: true, description: '', name: 'deployToProd')]
//
//    node('slave') {
//        if (deployToProd) {
//            def mvnHome = tool 'mvn 3.3.9'
//            wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1425975759468', targetLocation: 'settings.xml', variable: '']]]) {
//                try {
//                	// start server and deploy updated application. We use a file stored here in Jenkins to handle integration server configuration
//                	wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'faacea35-8822-4f47-be58-f43d95428d74', targetLocation: 'prodnos-server.xml', variable: '']]]) {
//                		sh "${mvnHome}/bin/mvn -s settings.xml -Pprod liberty:clean-server liberty:stop-server liberty:start-server -DconfigFile=prodnos-server.xml -Dtimeout=120"    
//                	}
//                } catch (all) {
//                	hipchatSend color: 'RED', message: "${env.JOB_NAME} #${env.BUILD_NUMBER} Build failed on stage Production Deploy (<a href=\"${env.BUILD_URL}\">Open</a>)", room: hipChatRoom, v2enabled: false, notify: true
//                }
//            }
//        }
//    }
//}

@NonCPS
def version() {
    def matcher = readFile('pom.xml') =~ /<version>(\d+)\.(\d+)\.?(\d+)?.*<\/version>/
    
    if (!matcher) error 'Version'
    
    def major = matcher[0][1]
    if (!major) error 'Version'
    
    def minor = matcher[0][2]
    if (!minor) error 'Version'
    
    def patch = matcher[0][3]
    
    def full = "${major}.${minor}"
    full += patch ? ".${patch}": ""
    
    ['major': major, 'minor': minor, 'patch': patch, 'full': full]
}

@NonCPS
def file(String base) {
    // we grab first file cause there should be a single war
    new FileParameterValue.FileItemImpl(new File(new FileNameFinder().getFileNames(base, '**/*.war')[0]))
}
