package com.example.roadguideapp.auth



import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



@Composable

internal fun AuthOverlayHost(

    destination: AuthDestination,

    onNavigate: (AuthDestination) -> Unit,

    onDismiss: () -> Unit,

    onAuthChanged: () -> Unit,

    modifier: Modifier = Modifier,

) {

    val context = LocalContext.current

    var friendsRevision by remember { mutableIntStateOf(0) }

    val friends = remember(friendsRevision) { OfflineFriendsStore.listFriends(context) }

    val friendsCount = friends.size

    val onFriendsChanged: () -> Unit = { friendsRevision++ }

    LaunchedEffect(destination) {
        if (destination == AuthDestination.Friends ||
            destination == AuthDestination.UserProfile ||
            destination == AuthDestination.AddFriendMenu
        ) {
            withContext(Dispatchers.IO) {
                OfflineFriendsStore.refreshFromBackend(context)
            }
            friendsRevision++
        }
    }

    when (destination) {

        AuthDestination.SignIn -> {

            SignInScreen(

                modifier = modifier,

                onBack = onDismiss,

                onSignedIn = {

                    onAuthChanged()

                    onDismiss()

                },

                onCreateAccount = { onNavigate(AuthDestination.SignUp) },

            )

        }



        AuthDestination.SignUp -> {

            SignUpScreen(

                modifier = modifier,

                onBack = {

                    onNavigate(

                        if (OfflineAuthStore.isSessionActive(context)) {

                            AuthDestination.UserProfile

                        } else {

                            AuthDestination.SignIn

                        },

                    )

                },

                onSignedUp = {

                    onAuthChanged()

                    onDismiss()

                },

            )

        }



        AuthDestination.ResetCredentials -> {

            val initialId = OfflineAuthStore.sessionIdentifier(context)

                ?: OfflineAuthStore.storedIdentifier(context).orEmpty()

            ResetCredentialsScreen(

                modifier = modifier,

                initialIdentifier = initialId,

                onBack = {

                    if (OfflineAuthStore.isSessionActive(context)) {

                        onNavigate(AuthDestination.UserProfile)

                    } else {

                        onNavigate(AuthDestination.SignIn)

                    }

                },

                onCredentialsUpdated = {

                    onAuthChanged()

                    onDismiss()

                },

            )

        }



        AuthDestination.UserProfile -> {

            val identifier = OfflineAuthStore.sessionIdentifier(context).orEmpty()

            val profileId = OfflineAuthStore.profileId(context).orEmpty()

            UserProfileScreen(

                modifier = modifier,

                identifier = identifier,

                profileId = profileId,

                abbreviation = identifierAbbreviation(identifier),

                friendsCount = friendsCount,

                onBack = onDismiss,

                onResetCredentials = { onNavigate(AuthDestination.ResetCredentials) },

                onCreateNewAccount = {

                    OfflineAuthStore.clearAccount(context)

                    onAuthChanged()

                    onNavigate(AuthDestination.SignUp)

                },

                onSignOut = {

                    OfflineAuthStore.endSession(context)

                    onAuthChanged()

                    onDismiss()

                },

                onMyQrCode = { onNavigate(AuthDestination.MyQrCode) },

                onAddFriend = { onNavigate(AuthDestination.AddFriendMenu) },

                onFriendsList = { onNavigate(AuthDestination.Friends) },

            )

        }



        AuthDestination.AddFriendMenu -> {

            AddFriendMenuScreen(

                modifier = modifier,

                onBack = { onNavigate(AuthDestination.UserProfile) },

                onScanQr = { onNavigate(AuthDestination.ScanFriendQr) },

                onAddById = { onNavigate(AuthDestination.AddFriendById) },

            )

        }



        AuthDestination.Friends -> {

            FriendsListScreen(

                modifier = modifier,

                friends = friends,

                onBack = { onNavigate(AuthDestination.UserProfile) },

                onFriendRemoved = onFriendsChanged,

            )

        }



        AuthDestination.MyQrCode -> {

            val profileId = OfflineAuthStore.profileId(context).orEmpty()

            val displayName = OfflineAuthStore.sessionIdentifier(context).orEmpty()

            MyQrCodeScreen(

                modifier = modifier,

                profileId = profileId,

                displayName = displayName,

                onBack = { onNavigate(AuthDestination.UserProfile) },

            )

        }



        AuthDestination.ScanFriendQr -> {

            ScanFriendQrScreen(

                modifier = modifier,

                onBack = { onNavigate(AuthDestination.AddFriendMenu) },

                onFriendAdded = {

                    onFriendsChanged()

                    onNavigate(AuthDestination.UserProfile)

                },

            )

        }



        AuthDestination.AddFriendById -> {

            AddFriendByIdScreen(

                modifier = modifier,

                onBack = { onNavigate(AuthDestination.AddFriendMenu) },

                onFriendAdded = {

                    onFriendsChanged()

                    onNavigate(AuthDestination.UserProfile)

                },

            )

        }

    }

}


