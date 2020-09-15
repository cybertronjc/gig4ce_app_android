/*jshint esversion: 6 */

const functions = require('firebase-functions');
const firebaseAdmin = require('firebase-admin');

firebaseAdmin.initializeApp();
const db = firebaseAdmin.firestore();

function sendNotificationForCheckout() {

    const gigCollectionRef = db.collection('Gigs');

    var checkInTimeStartWithStartTolreance = new Date();
    checkInTimeStartWithStartTolreance.setMinutes(startOfToday.getMinutes - 10);

    gigCollectionRef
        .where('attendance', '!=', "null")
        .where('attendance.checkOutTime', '==', 'null')
        .where('endDateTime', '>=', checkInTimeStartWithStartTolreance)
        .where('endDateTime', '<=', Date())
        .get()
        .then(docList => {
            console.log("sendNotificationForMissedCheckin : got ${docList.size} Gig entries");

            if (docList.empty) {
                console.log("sendNotificationForMissedCheckin : got no entries from gig collection");
            } else {
                fetchFirebaseTokensAndSendNotifcation(docList);
            }
        })
        .catch(err => {
            console.log("err while getting getting verification entries", err);
        });
}

function fetchFirebaseTokensAndSendNotifcation(gigsWithPendingCheckout) {
    const firebaseTokensRef = db.collection('firebase_tokens');

    const gigersWithPendingCheckouts = gigsWithPendingCheckout.map(gig => {
        gig.gigerId
    });

    firebaseTokensRef
        .where('uid', 'in', gigersWithPendingCheckouts)
        .get()
        .then(docList => {
            sendNotification(docList, gigsWithPendingCheckout);
        })
        .catch(err => {

        });
}

function sendNotification(notificationTokens, gigsWithPendingCheckout) {
    const firebaseTokensRef = db.collection('firebase_tokens');

    const gigersWithPendingCheckouts = gigsWithPendingCheckout.map(gig => {
        gig.gigerId
    });

    const tokenUserIdMap = Map();
    notificationTokens.forEach(it => {
        tokenUserIdMap.set(it.uid, it.id);
    });

    gigsWithPendingCheckout.forEach(it => {
        it.firebaseToken = tokenUserIdMap.get(it.gigerId);
    });

    const distinctCompanies = [...new Set(gigsWithPendingCheckout.map(it => it.companyName))];
    const groupedGigs = groupBy(gigsWithPendingCheckout, gig => gig.companyName);


    distinctCompanies.forEach(it => {

        const gigs = groupedGigs.get(it);
        if (gigs.empty) {
            console.log('No gigs found for company $it');
        } else {

            const role = gig[0].role;

            const message = {
                notification: {
                    title: 'Attendance check out Alert',
                    body: 'Hey You can check out for $role@ $it now'
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