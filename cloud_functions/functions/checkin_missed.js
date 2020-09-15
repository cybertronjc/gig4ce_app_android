/*jshint esversion: 6 */

const functions = require('firebase-functions');
const firebaseAdmin = require('firebase-admin');

firebaseAdmin.initializeApp();
const db = firebaseAdmin.firestore();

function sendNotificationForCheckout() {

    const gigCollectionRef = db.collection('Gigs');

    var startOfToday = new Date();
    startOfToday.setHours(0, 0, 0, 0);
    var endOfToday = new Date();
    endOfToday.setHours(23, 59, 59, 999);

    var checkInTimeStartWithStartTolreance = new Date();
    checkInTimeStartWithStartTolreance.setMinutes(startOfToday.getMinutes - 10);

    gigCollectionRef
        .where('attendance', '==', 'null')
        .where('startDateTime', '>', startOfToday)
        .where('startDateTime', '<=', Date())
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


    const gigersWithPendingCheckIns = gigsWithPendingCheckout
        .filter(it => {
            const startDateTimeWithTolerance = it.startDateTime;
            startDateTimeWithTolerance.setsetMinutes(it.startDateTime.getMinutes() + it.checkInAfterTimeBufferInMins);

            var secondDiffs = (Date().getTime() - startDateTimeWithTolerance.getTime()) / 1000;
            return secondDiffs > 0 && secondDiffs <= 600;
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
                    title: 'Attendance Alert',
                    body: 'You are marked absent for the day. Please contact your supervisor if any issue'
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