/*jshint esversion: 6 */

const functions = require('firebase-functions');
const firebaseAdmin = require('firebase-admin');

firebaseAdmin.initializeApp();
const db = firebaseAdmin.firestore();

function sendNotificationForCheckIn() {

    const gigCollectionRef = db.collection('Gigs');

    var startOfToday = new Date();
    startOfToday.setHours(0, 0, 0, 0);
    var currentTime = new Date();

    var checkInTimeStartWithStartTolreance = new Date();
    checkInTimeStartWithStartTolreance.setMinutes(startOfToday.getMinutes - 10);

    gigCollectionRef
        .where('attendance', '==', 'null')
        .where('startDateTime', '>', startOfToday)
        .where('startDateTime', '<=', currentTime)
        .get()
        .then(docList => {
            console.log("sendNotificationForMissedCheckin : got ${docList.size} Gig entries");

            if (docList.empty) {
                console.log("sendNotificationForMissedCheckin : got no entries from gig collection");
            } else {
                filterGigsAndSendNotification(docList);
            }
        })
        .catch(err => {
            console.log("err while getting getting verification entries", err);
        });
}

function filterGigsAndSendNotification(gigsWithPendingCheckIn) {
    const firebaseTokensRef = db.collection('firebase_tokens');


    const gigersWithPendingCheckIns = gigsWithPendingCheckIn
        .filter(it => {
            const startDateTimeWithTolerance = it.startDateTime;
            startDateTimeWithTolerance.setsetMinutes(it.startDateTime.getMinutes() - it.checkInBeforeTimeBufferInMins);

            var secondDiffs = (startDateTimeWithTolerance.getTime() - Date().getTime()) / 1000;
            return secondDiffs >= 0 && secondDiffs <= 600;
        }).map(gig => {
            gig.gigerId
        });

    firebaseTokensRef
        .where('uid', 'in', gigersWithPendingCheckIns)
        .get()
        .then(docList => {
            sendNotification(docList, gigersWithPendingCheckIns);
        })
        .catch(err => {

        });
}

function sendNotification(notificationTokens, gigersWithPendingCheckIn) {
    const firebaseTokensRef = db.collection('firebase_tokens');

    const gigersWithPendingCheckIns = gigersWithPendingCheckIn.map(gig => {
        gig.gigerId
    });

    const tokenUserIdMap = Map();
    notificationTokens.forEach(it => {
        tokenUserIdMap.set(it.uid, it.id);
    });

    gigersWithPendingCheckIns.forEach(it => {
        it.firebaseToken = tokenUserIdMap.get(it.gigerId);

        if (gigs.empty) {
            console.log('No pending gigs found');
        } else {

            // {
            //     "to": "ckDK1dTtTmOuxtHa5pnMYG:APA91bEFcKAyWAI-gBydC_xEw0cOa5wQz5i-2tG6cG1eNs6Z9ohRzWsSMCT7HtSTWCUKk2pJ5fCL5naMkjoaqRD2CyhWxL-ial-eb13RtqWqD_OMscW5OjVzVmQCzJGaDmjO3TXUpU34",
            //     "collapse_key": "type_a",
            //     "notification": {
            //       "title": "Gig Checkin",
            //       "body": "Hey Himanshu, Please check in for Sales Executive @ Abc now.",
            //       "click_action": "com.gigforce.app.gig.open_gig_attendance_page",
            //       "sound": "default"
            //     },
            //     "data": {
            //       "gig_id": "0FmrW6mIKNKBt9etIGkt",
            //       "is_deeplink": true
            //     }
            //   }

            const role = it.role;
            const companyName = it.companyName;

            const message = {
                notification: {
                    title: 'Attendance Check-in Alert',
                    body: 'Hey, Please check in for $role@ $companyName now',
                    click_action: "com.gigforce.app.gig.open_gig_attendance_page",
                    sound: "default"
                },
                data: {
                    is_deeplink: true,
                    gig_id: it.id
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
    });

}