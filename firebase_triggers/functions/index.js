const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendFriendRequest = functions.database.ref('FriendRequests/{user1_id}/{user2_id}')
.onCreate(event => {
	const user_id = event.params.user1_id;
	const from_user_id = event.params.user2_id;
	const request = admin.database().ref(`FriendRequests/${user_id}/${from_user_id}`).once('value');
	return request.then(requestResult => {
		const request_type = requestResult.val().request_type;

		if(request_type === "sent"){
			admin.database().ref(`FriendRequests/${from_user_id}/${user_id}`).set({
				request_type: "received"
			});
		}

		if(request_type === "received"){
			const userNameValue = admin.database().ref(`/CommonUsers/${from_user_id}/name`).once('value');
			const deviceToken = admin.database().ref(`/CommonUsers/${user_id}/device_token`).once('value');

			return Promise.all([userNameValue, deviceToken]).then(result => {
				const userName = result[0].val();
				const token_id = result[1].val();
				if(token_id !== null) {
					const payload = {
						notification: {
							title : `${userName}`,
							body: `${userName} has sent you friend request.`,
							icon: "default",
							click_action: "com.example.jdachuk.meetin_FRIEND_REQUEST_ACTION"
						},
						data : {
							user_id : from_user_id,
							type : "friend request"
						}
					};

					return admin.messaging().sendToDevice(token_id, payload).then(response => {
							return console.log('This was the notification feature');
					});
				} else{
          return console.log('Error while sending notification.');
        }
			});
		}
		return console.log('Request sent.');
	});
});

exports.AddFriend = functions.database.ref('Friends/{user1_id}/{user2_id}')
.onCreate(event => {
	const user1_id = event.params.user1_id;
	const user2_id = event.params.user2_id;
	const globalUpdates = {};

	return admin.database().ref(`Friends/${user1_id}/${user2_id}`).once('value').then(friendResult => {
		globalUpdates[`Friends/${user2_id}/${user1_id}`] = friendResult.val();
		globalUpdates[`FriendRequests/${user1_id}/${user2_id}`] = null;

		return admin.database().ref().update(globalUpdates);
	});
});
