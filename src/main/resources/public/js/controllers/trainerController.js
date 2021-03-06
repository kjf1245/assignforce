var assignforce = angular.module( "batchApp" );

assignforce.controller( "trainerCtrl", function( $scope, $rootScope, $mdDialog, $mdToast, $location, trainerService, s3Service, ptoService, $http) {
    var tc = this;
    $scope.isManager = $rootScope.role === "VP of Technology";

      // functions

    // calls showToast method of aCtrl
    tc.showToast = function( message ) {
        $scope.$parent.aCtrl.showToast( message );
    };

    //adds a trainer by popping up a dialog box
    tc.addTrainer = function () {
        $mdDialog.show({
            templateUrl: "html/templates/dialogs/trainerDialog.html",
            controller: "trainerDialogCtrl",
            controllerAs: "tdCtrl",
            locals: {
                trainer : trainerService.getEmptyTrainer(),
                state   : "create" },
            bindToController: true,
            clickOutsideToClose: true
        }).then(function () {
            tc.showToast("Trainer Added.");
            tc.rePullTrainers();
        }, function () {
            tc.showToast("Failed to add trainer.")
        });
    };

    //deactivates a single trainer
    tc.removeTrainer = function (trainerRM) {
        trainerRM.active = false; //set active to false deactivating the trainer in the front end

        //calls the update method to set active to false in the database.
        trainerService.update(trainerRM, function () {
            tc.showToast(trainerRM.firstName +" "+ trainerRM.lastName + " was removed successfully.");
        }, function () {
            tc.showToast("Failed to remove " + trainerRM.firstName +" "+ trainerRM.lastName);
        });
    };

    //connects to aws s3 to grab an object
    tc.grabS3Resume = function (trainer) {
        var fileName = trainer.resume; //this is used to hide the filename and update button on the HTML side

        //if the trainer has a null resume in the database then it will
        // show the toast and stop running the function
        if(fileName == null){
            tc.showToast(trainer.firstName + " " + trainer.lastName + " does not have any resume uploaded.");
            return;
        }

        //This initializes a bucket with the keys obtained from Creds rest controller
        var bucket = new AWS.S3({
            accessKeyId: tc.creds.ID,
            secretAccessKey: tc.creds.SecretKey,
            region: 'us-east-1'
        });

        //set the parameters needed to get an object from aws s3 bucket
        var params = {
            Bucket: tc.creds.BucketName,
            Key: fileName,
            Expires: 60 //url expires in 60 seconds with signed urls
        };

        //grabs a url to the object in the s3 bucket
        tc.url = bucket.getSignedUrl('getObject', params);

        //this will create a link, set download and href, and invoke the click action on it
        // it will download the file
        var link = document.createElement("a");
        link.download = "test.png";
        link.href = tc.url;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    //activates a trainer
    tc.activateTrainer = function (trainer) {
        trainer.active = true;
        trainerService.update(trainer, function () {
            tc.showToast(trainer.firstName +" "+ trainer.lastName + " Activated")
        }, function () {
            tc.showToast("Unable to activate " + trainer.firstName +" "+ trainer.lastName);
        })
    };

    // reformats how an array of objects is joined
    tc.joinObjArrayByName = function(elem) {
        return elem.name;
    };

    // redirects the url to go to the profile page
    // appends the trainer id so that the profile page can load that id in
    tc.goToTrainer = function(event) {
        var id = event.trainerId;
        $location.path('/profile/' + id);
    };

    //queries the database for trainers. to be called after a change to the trainers array
    tc.rePullTrainers = function(){
        tc.trainers = undefined;
        trainerService.getAll( function(response) {
            tc.trainers = response;
        }, function() {
            tc.showToast("Could not fetch trainers.");
        });
    };

    tc.convertUnavailability = function(incoming){
        return new Date(incoming);
    };

    tc.showCalendar = function(){
        $http.get("/api/v2/google/googleStatus").then(function(response) {
            if(response.data !== "") {
                ptoService.authorize();
            } else {
                 tc.googleAuth();
             }
        });
    };

    tc.hideCalendar = function(){
        $mdDialog.cancel();
    };

    tc.showPTODialog = function(){
        $mdDialog.show({
            templateUrl: "html/templates/dialogs/ptoRequestDialog.html",
            controller: "ptoCtrl",
            controllerAs: "ptoCtrl",
            bindToController: true,
            clickOutsideToClose: true
        });
    };

    // page initialization

    //get the S3 bucket credentials and store them in creds
    s3Service.getCreds(function (response) {
        tc.creds = response;
    }, function () {
        tc.showToast("failed to get credentials")
    });

    // gets all trainers and stores them in variable trainers
    trainerService.getAll( function(response) {
        tc.trainers = response;
    }, function() {
        tc.showToast("Could not fetch trainers.");
    });

    tc.googleAuth = function() {
        window.location = "api/v2/google/google";
    }

});//end trainer controller