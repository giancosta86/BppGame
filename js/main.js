var baseURL =
  "https://github.com/giancosta86/BppGame/releases/latest"



function onMobileDevice() {
  return window.screen.availWidth < 840
}


window.onload = function() {
  if (!onMobileDevice()) {
    setupRunWithMoonDeployButton()
    setupDownloadBinaryZipButton()
  }
}



function setupRunWithMoonDeployButton() {
  getLatestMoonDescriptor(
    baseURL,

    "App.moondeploy",

    function(descriptorURL) {
      var runButton = document.getElementById("runGame")
      runButton.href = descriptorURL
      runButton.style.display = "inline-block"
    },

    function(statusCode) {
      console.log("Error while retrieving the MoonDeploy descriptor")
      console.log(statusCode)
    }
  )
}



function setupDownloadBinaryZipButton() {
  getLatestReleaseArtifact(
    baseURL,

    "BppGame",

    ".zip",

    function(zipURL) {
      var downloadBinaryZipButton = document.getElementById("downloadGame")
      downloadBinaryZipButton.href = zipURL
      downloadBinaryZipButton.style.display = "inline-block"
    },

    function(statusCode) {
      console.log("Error while retrieving the binary zip artifact")
      console.log(statusCode)
    }
  )
}
