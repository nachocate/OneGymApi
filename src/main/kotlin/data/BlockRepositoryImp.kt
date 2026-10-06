package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.BlockDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.Block
import com.concatstudio.onegym.respository.BlockRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class BlockRepositoryImp : BlockRepository {
    override fun getBlocks() = transaction(Database.connection) { BlockDao.all().map(::toModel) }
    override fun getBlockById(id: Long) = transaction(Database.connection) { BlockDao.findById(id)?.let(::toModel) }
    override fun updateBlock(blockId: Long, block: Block) = transaction(Database.connection) { BlockDao.findById(blockId)?.apply { position = block.position; name = block.name; dayId = dayRef(block.dayId); laps = block.laps }; Unit }
    override fun createBlock(block: Block) = transaction(Database.connection) { BlockDao.new { position = block.position; name = block.name; dayId = dayRef(block.dayId); laps = block.laps }; Unit }
    override fun deleteBlockById(id: Long) = transaction(Database.connection) { BlockDao.findById(id)?.delete(); Unit }
}
