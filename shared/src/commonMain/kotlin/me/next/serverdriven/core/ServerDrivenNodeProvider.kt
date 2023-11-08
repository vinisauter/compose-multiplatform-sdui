package me.next.serverdriven.core

import me.next.serverdriven.core.library.SDLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode

interface ServerDrivenNodeProvider{
     val library: SDLibrary
     val node: ServerDrivenNode
}