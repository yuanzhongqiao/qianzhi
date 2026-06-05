package com.llmhub.llmhub.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llmhub.llmhub.data.ChatEntity
import com.llmhub.llmhub.viewmodels.ChatDrawerViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.llmhub.llmhub.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDrawer(
    onNavigateToChat: (String) -> Unit,
    onCreateNewChat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToModels: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCreatorChat: (String) -> Unit,
    onClearAllChats: (() -> Unit)? = null,
    viewModel: ChatDrawerViewModel = viewModel()
) {
    val chats by viewModel.allChats.collectAsState()
    val creators by viewModel.allCreators.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var chatToRename by remember { mutableStateOf<ChatEntity?>(null) }
    var creatorToEdit by remember { mutableStateOf<com.llmhub.llmhub.data.CreatorEntity?>(null) }
    var chatToDelete by remember { mutableStateOf<ChatEntity?>(null) }
    var creatorToDelete by remember { mutableStateOf<com.llmhub.llmhub.data.CreatorEntity?>(null) }
    
    // ... dialog logic ...
    
    if (chatToRename != null) {
        com.llmhub.llmhub.screens.RenameChatDialog(
            chatTitle = chatToRename!!.title,
            onConfirm = { newTitle ->
                viewModel.renameChat(chatToRename!!.id, newTitle)
                chatToRename = null
            },
            onDismiss = { chatToRename = null }
        )
    }

    creatorToEdit?.let { creator ->
        CreatorEditDialog(
            creator = creator,
            onConfirm = { updated ->
                viewModel.updateCreator(updated)
                creatorToEdit = null
            },
            onDismiss = { creatorToEdit = null }
        )
    }

    if (chatToDelete != null) {
        AlertDialog(
            onDismissRequest = { chatToDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_chat_title)) },
            text = { Text(stringResource(R.string.dialog_delete_chat_message, chatToDelete!!.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteChat(chatToDelete!!.id)
                        chatToDelete = null
                    }
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { chatToDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    creatorToDelete?.let { creator ->
        AlertDialog(
            onDismissRequest = { creatorToDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_creator_title)) },
            // Avoid localized format placeholder crashes in some locales where `%1` is invalid.
            text = { Text(creator.name) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCreator(creator)
                        creatorToDelete = null
                    }
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { creatorToDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_all_chats_title)) },
            text = { Text(stringResource(R.string.dialog_delete_all_chats_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllChats?.invoke()
                        showDeleteAllDialog = false
                    }
                ) { Text(stringResource(R.string.action_delete_all)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header with Back Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.drawer_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // New Chat Button
            FilledTonalButton(
                onClick = onCreateNewChat,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.drawer_new_chat))
            }
            
            // Creators Section
            if (creators.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.drawer_my_creators),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp), // Limit height so it doesn't take over
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(creators) { creator ->
                        CreatorItem(
                            creator = creator,
                            onClick = { onNavigateToCreatorChat(creator.id) },
                            onDelete = { creatorToDelete = creator },
                            onRename = { creatorToEdit = creator }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chat History header
            Text(
                text = stringResource(R.string.drawer_recent_chats),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Chat history items: dedicated scroll area. Use a fairly small weight so the
            // action/navigation area can remain visible in landscape; bottom actions are
            // allowed to scroll if space is constrained.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(chats) { chat ->
                    ChatHistoryItem(
                        chat = chat,
                        onClick = { onNavigateToChat(chat.id) },
                        onDelete = { chatToDelete = chat },
                        onRename = { chatToRename = chat }
                    )
                }
                if (chats.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.drawer_no_chats),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Divider()

            // Navigation Options pinned at bottom. Make this block scrollable when
            // vertical space is constrained (especially in landscape) so items don't get
            // truncated and the chat list can remain visible.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp)
            ) {
                DrawerNavigationItem(
                    icon = Icons.Default.GetApp,
                    text = stringResource(R.string.drawer_download_models),
                    onClick = onNavigateToModels
                )

                DrawerNavigationItem(
                    icon = Icons.Outlined.DeleteSweep,
                    text = stringResource(R.string.drawer_clear_all_chats),
                    onClick = { showDeleteAllDialog = true }
                )

                DrawerNavigationItem(
                    icon = Icons.Default.Settings,
                    text = stringResource(R.string.drawer_settings),
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatorEditDialog(
    creator: com.llmhub.llmhub.data.CreatorEntity,
    onConfirm: (com.llmhub.llmhub.data.CreatorEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var icon by remember(creator.id) { mutableStateOf(creator.icon) }
    var name by remember(creator.id) { mutableStateOf(creator.name) }
    var description by remember(creator.id) { mutableStateOf(creator.description) }
    var pctfPrompt by remember(creator.id) { mutableStateOf(creator.pctfPrompt) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.creator_screen_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it.take(2) },
                    modifier = Modifier.width(100.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.chat_title)) }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    label = { Text(stringResource(R.string.creator_prompt_label)) }
                )
                Text(
                    text = stringResource(R.string.creator_system_prompt_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = pctfPrompt,
                    onValueChange = { pctfPrompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 8
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        creator.copy(
                            icon = icon.trim().ifBlank { creator.icon },
                            name = name.trim().ifBlank { creator.name },
                            description = description.trim().ifBlank { creator.description },
                            pctfPrompt = pctfPrompt.trim().ifBlank { creator.pctfPrompt }
                        )
                    )
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun ChatHistoryItem(
    chat: ChatEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatTimestamp(chat.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_rename)) },
                        onClick = {
                            expanded = false
                            onRename()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerNavigationItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 1 -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
        days == 1L -> stringResource(R.string.time_yesterday)
        hours > 0 -> stringResource(R.string.time_hours_ago, hours.toInt())
        minutes > 0 -> stringResource(R.string.time_minutes_ago, minutes.toInt())
        else -> stringResource(R.string.time_just_now)
    }
}
