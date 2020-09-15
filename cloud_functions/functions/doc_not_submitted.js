/*jshint esversion: 6 */

const functions = require('firebase-functions');
const firebaseAdmin = require('firebase-admin');

firebaseAdmin.initializeApp();
const db = firebaseAdmin.firestore();

exports.sendNotificationWithPendingVerfication = functions.region('asia-south1').https.onCall((data, context) => {
    console.log("sendNotificationWithPendingVerfication : Invoked");

    const verificationCollectionRef = db.collection('Verification');

    verificationCollectionRef
        .get()
        .then(docList => {
            console.log("sendNotificationWithPendingVerfication : got ${docList.size} verification entries")

            if (docList.empty) {

                console.log("sendNotificationWithPendingVerfication : got no entries from verification collection")
            } else {
                const userWhoHaveUploadedverificationData = docList
                    .filter(checkVerificationDocumentsUploaded)
                    .map(doc => {
                        doc.id
                    });
                compareAndSendUploadDocumentNotification(userWhoHaveUploadedverificationData);
            }
        })
        .catch(err => {
            console.log("err while getting getting verification entries", err);
        });

});

function checkVerificationDocumentsUploaded(verificationDocument) {
    const aadharUploaded = verificationDocument.aadhar_card != null
        && verificationDocument.aadhar_card.frontImage != null;

    const bankDetailsUploaded = verificationDocument.bank_details != null
        && verificationDocument.bank_details.frontImage != null;

    const drivingLicenseDetailsUploaded = verificationDocument.driving_license != null
        && verificationDocument.driving_license.frontImage != null;

    const panCardDetailsUploaded = verificationDocument.pan_card != null
        && verificationDocument.pan_card.panCardImagePath != null;

    const selfieUploaded = verificationDocument.selfie_video != null
        && verificationDocument.selfie_video.videoPath != null;

    return selfieUploaded && panCardDetailsUploaded && bankDetailsUploaded && (aadharUploaded || drivingLicenseDetailsUploaded)
}

function compareAndSendUploadDocumentNotification(userWhoHaveUploadedverificationData) {
    firebaseAdmin
        .auth()
        .listUsers()
        .then(users => {

            var uidOfUsersWithPendingDocumentUpload = users.map(user => {
                user.uid
            }).filter(uid => {
                return !userWhoHaveUploadedverificationData.includes(uid);
            });

            fetchFbTokensAndSendNotifcation(uidOfUsersWithPendingDocumentUpload);
        })
        .catch(err => {
            console.log("err while getting users", err);
        });
}

function fetchFbTokensAndSendNotifcation(userWhoHaveUploadedverificationData) {

    const firebaseTokensRef = db.collection('firebase_tokens');
    firebaseTokensRef
        .where('uid', 'in', userWhoHaveUploadedverificationData)
        .get()
        .then(users => {

            const firebaseTokens = users.map(user=> {
                user.id
            });

            sendNotification(firebaseTokens);
        })
        .catch(err => {
            console.log("err while fetching firebase tokens", err);
        });
}

function sendNotification(registrationTokens) {

    const message = {
        notification: {
            title: 'Verification Incomplete',
            body: 'Hey, Please upload your documents to activate profile.'
        },
        tokens: registrationTokens,
    };

    admin.messaging().send(message)
        .then((response) => {
            // Response is a message ID string.
            console.log('Successfully sent message:', response);
        })
        .catch((error) => {
            console.log('Error sending message:', error);
        });

}