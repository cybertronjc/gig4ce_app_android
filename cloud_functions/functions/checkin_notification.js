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
    });

    const distinctCompanies = [...new Set(gigersWithPendingCheckIns.map(it => it.companyName))];
    const groupedGigs = groupBy(gigersWithPendingCheckIns, gig => gig.companyName);


    distinctCompanies.forEach(it => {

        const gigs = groupedGigs.get(it);
        if (gigs.empty) {
            console.log('No gigs found for company $it');
        } else {

            const role = gig[0].role;

            const message = {
                notification: {
                    title: 'Attendance Check-in Alert',
                    body: 'Hey, Please check in for $role@ $it now'
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


function groupBy(list, keyGetter) {
    const map = new Map();
    list.forEach((item) => {
        const key = keyGetter(item);
        const collection = map.get(key);
        if (!collection) {
            map.set(key, [item]);
        } else {
            collection.push(item);
        }
    });
    return map;
}