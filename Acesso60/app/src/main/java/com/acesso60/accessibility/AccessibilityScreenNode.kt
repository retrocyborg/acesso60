package com.acesso60.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import br.com.acesso60.core.screen.ScreenNode

/** Adaptador de [AccessibilityNodeInfo] para o contrato [ScreenNode] do núcleo. */
class AccessibilityScreenNode(private val node: AccessibilityNodeInfo) : ScreenNode {
    override val text: String? get() = node.text?.toString()
    override val contentDescription: String? get() = node.contentDescription?.toString()
    override val viewId: String? get() = node.viewIdResourceName
    override val isPassword: Boolean get() = node.isPassword
    override val isVisible: Boolean get() = node.isVisibleToUser
    override val children: Sequence<ScreenNode> get() = sequence {
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            yield(AccessibilityScreenNode(child))
        }
    }
}
