package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.Block

interface BlockRepository {
    fun getBlocks(): List<Block>
    fun getBlockById(id: Long): Block?
    fun updateBlock(blockId: Long, block: Block)
    fun createBlock(block: Block)
    fun deleteBlockById(id: Long)
}
