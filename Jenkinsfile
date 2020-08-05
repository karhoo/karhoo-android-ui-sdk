@Library(value="service-builder-shared-library@master", changelog=false) _

androidCICD {
    rootDir = "KarhooTraveller"
    successNotificationChannel = "release-notifications"
    failureNotificationChannel = "release-notifications"
    developLanes = [
        //"test",
        "testProdQAUnitTest",
        "testStagingUnitTest",
        //"testReleaseUnitTest",
        //"assemble",
        //"assembleAndroidTest",
        "assembleProdQA",
        //"assembleRelease",
        "assembleStaging",
    ]
    releaseLanes = [
        //"test",
        "testProdQAUnitTest",
        //"testReleaseUnitTest",
        "testStagingUnitTest",
        //"assemble,
        //"assembleAndroidTest",
        "assembleProdQA",
        //"assembleRelease",
        "assembleStaging",
    ]
    releaseHockeyAppLanes = [
        //"pushAll",
        "pushProdQA",
        "pushStaging",
    ]
    masterLanes = [

    ]
}
