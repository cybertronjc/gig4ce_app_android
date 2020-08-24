const functions = require('firebase-functions');
const firebaseAdmin = require('firebase-admin');

const SCREEN_LANDING = "landing"
const SCREEN_CALENDAR_HOME_SCREEN = "calendar_home_screen"

firebaseAdmin.initializeApp()
const db = firebaseAdmin.firestore()

const gigCollectionRef = db
    .collection('Gigs')

exports.getLandingScreenRedirectionConfig = functions.region('asia-south1').https.onCall((data, context) => {
    console.log("getMainScreenRedirectionConfig : Invoked")

    // Checking that the user is authenticated.
    if (!context.auth) {

        console.log("getMainScreenRedirectionConfig : auth Error")

        // Throwing an HttpsError so that the client gets the error details.
        throw new functions.https.HttpsError('failed-precondition', 'The function must be called ' +
            'while authenticated.');
    }

    var dat = {
        helloWorld: "firstNumber",
        secondNumber: "secondNumber",
        operator: '+',
        operationResult: "firstNumber + secondNumber"
    };

    const uid = context.auth.uid;
    console.log("getMainScreenRedirectionConfig : UID " + uid)


    gigCollectionRef.where('gigerId', '==', uid)
        .get()
        .then(docList => {
            console.log("Sending Back", dat)

            if (docList.empty) {
                return dat;
            } else {
                return dat;
            }
        })
        .catch(err => {
            console.log("err while getting getMainScreenRedirectionConfig", err)

            console.log("Sending Back", dat)
            return dat;
        })

});