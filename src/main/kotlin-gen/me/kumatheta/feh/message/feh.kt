package me.kumatheta.feh.message

import kotlinx.serialization.Serializable

@Serializable
data class UnitAdded(
    val name: String,
    val unitId: Int,
    val maxHp: Int,
    val playerControl: Boolean,
    val startX: Int,
    val startY: Int,
    val moveType: MoveType,
    val attackType: AttackType
)

enum class MoveType {
    INFANTRY,
    ARMORED,
    CAVALRY,
    FLIER
}

enum class AttackType {
    SWORD,
    LANCE,
    AXE,
    RED_BOW,
    BLUE_BOW,
    GREEN_BOW,
    COLORLESS_BOW,
    RED_DAGGER,
    BLUE_DAGGER,
    GREEN_DAGGER,
    COLORLESS_DAGGER,
    RED_TOME,
    BLUE_TOME,
    GREEN_TOME,
    STAFF,
    RED_BREATH,
    BLUE_BREATH,
    GREEN_BREATH,
    COLORLESS_BREATH,
    RED_BEAST,
    BLUE_BEAST,
    GREEN_BEAST,
    COLORLESS_BEAST
}

@Serializable
data class Terrain(
    val type: Type,
    val isDefenseTile: Boolean
) {
    enum class Type {
        REGULAR,
        WALL,
        FLIER_ONLY,
        FOREST,
        TRENCH
    }
}

@Serializable
data class BattleMapPosition(
    val x: Int,
    val y: Int,
    val terrain: Terrain,
    val obstacle: Int
)

@Serializable
data class BattleMap(
    val sizeX: Int,
    val sizeY: Int,
    val battleMapPositions: List<BattleMapPosition> = emptyList()
)

@Serializable
data class SetupInfo(
    val battleMap: BattleMap,
    val unitsAdded: List<UnitAdded>
)
//data class Action(
//    val unitId: Int = 0,
//    val moveX: Int = 0,
//    val moveY: Int = 0,
//    val targetUnitId: Int = 0,
//    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
//) : pbandk.Message<Action> {
//    override operator fun plus(other: Action?) = protoMergeImpl(other)
//    override val protoSize by lazy { protoSizeImpl() }
//    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
//    override fun jsonMarshal(json: Json) = jsonMarshalImpl(json)
//    fun toJsonMapper() = toJsonMapperImpl()
//    companion object : pbandk.Message.Companion<Action> {
//        val defaultInstance by lazy { Action() }
//        override fun protoUnmarshal(u: pbandk.Unmarshaller) = Action.protoUnmarshalImpl(u)
//        override fun jsonUnmarshal(json: Json, data: String) = Action.jsonUnmarshalImpl(json, data)
//    }
//
//    @Serializable
//    data class JsonMapper (
//        @SerialName("unitId")
//        val unitId: Int? = null,
//        @SerialName("moveX")
//        val moveX: Int? = null,
//        @SerialName("moveY")
//        val moveY: Int? = null,
//        @SerialName("targetUnitId")
//        val targetUnitId: Int? = null
//    ) {
//        fun toMessage() = toMessageImpl()
//    }
//}
//
//data class UnitUpdate(
//    val unitId: Int = 0,
//    val currentHp: Int = 0,
//    val active: Boolean = false,
//    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
//) : pbandk.Message<UnitUpdate> {
//    override operator fun plus(other: UnitUpdate?) = protoMergeImpl(other)
//    override val protoSize by lazy { protoSizeImpl() }
//    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
//    override fun jsonMarshal(json: Json) = jsonMarshalImpl(json)
//    fun toJsonMapper() = toJsonMapperImpl()
//    companion object : pbandk.Message.Companion<UnitUpdate> {
//        val defaultInstance by lazy { UnitUpdate() }
//        override fun protoUnmarshal(u: pbandk.Unmarshaller) = UnitUpdate.protoUnmarshalImpl(u)
//        override fun jsonUnmarshal(json: Json, data: String) = UnitUpdate.jsonUnmarshalImpl(json, data)
//    }
//
//    @Serializable
//    data class JsonMapper (
//        @SerialName("unitId")
//        val unitId: Int? = null,
//        @SerialName("currentHp")
//        val currentHp: Int? = null,
//        @SerialName("active")
//        val active: Boolean? = null
//    ) {
//        fun toMessage() = toMessageImpl()
//    }
//}
//
//data class UpdateInfo(
//    val action: me.kumatheta.feh.message.Action? = null,
//    val unitUpdates: List<me.kumatheta.feh.message.UnitUpdate> = emptyList(),
//    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
//) : pbandk.Message<UpdateInfo> {
//    override operator fun plus(other: UpdateInfo?) = protoMergeImpl(other)
//    override val protoSize by lazy { protoSizeImpl() }
//    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
//    override fun jsonMarshal(json: Json) = jsonMarshalImpl(json)
//    fun toJsonMapper() = toJsonMapperImpl()
//    companion object : pbandk.Message.Companion<UpdateInfo> {
//        val defaultInstance by lazy { UpdateInfo() }
//        override fun protoUnmarshal(u: pbandk.Unmarshaller) = UpdateInfo.protoUnmarshalImpl(u)
//        override fun jsonUnmarshal(json: Json, data: String) = UpdateInfo.jsonUnmarshalImpl(json, data)
//    }
//
//    @Serializable
//    data class JsonMapper (
//        @SerialName("action")
//        val action: me.kumatheta.feh.message.Action.JsonMapper? = null,
//        @SerialName("unitUpdates")
//        val unitUpdates: List<me.kumatheta.feh.message.UnitUpdate.JsonMapper> = emptyList()
//    ) {
//        fun toMessage() = toMessageImpl()
//    }
//}
//
//fun UnitAdded?.orDefault() = this ?: UnitAdded.defaultInstance
//
//private fun UnitAdded.protoMergeImpl(plus: UnitAdded?): UnitAdded = plus?.copy(
//    unknownFields = unknownFields + plus.unknownFields
//) ?: this
//
//private fun UnitAdded.protoSizeImpl(): Int {
//    var protoSize = 0
//    if (name.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(name)
//    if (unitId != 0) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.int32Size(unitId)
//    if (maxHp != 0) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.int32Size(maxHp)
//    if (playerControl) protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.boolSize(playerControl)
//    if (startX != 0) protoSize += pbandk.Sizer.tagSize(5) + pbandk.Sizer.int32Size(startX)
//    if (startY != 0) protoSize += pbandk.Sizer.tagSize(6) + pbandk.Sizer.int32Size(startY)
//    protoSize += unknownFields.entries.sumBy { it.value.size() }
//    return protoSize
//}
//
//private fun UnitAdded.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
//    if (name.isNotEmpty()) protoMarshal.writeTag(10).writeString(name)
//    if (unitId != 0) protoMarshal.writeTag(16).writeInt32(unitId)
//    if (maxHp != 0) protoMarshal.writeTag(24).writeInt32(maxHp)
//    if (playerControl) protoMarshal.writeTag(32).writeBool(playerControl)
//    if (startX != 0) protoMarshal.writeTag(40).writeInt32(startX)
//    if (startY != 0) protoMarshal.writeTag(48).writeInt32(startY)
//    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
//}
//
//private fun UnitAdded.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): UnitAdded {
//    var name = ""
//    var unitId = 0
//    var maxHp = 0
//    var playerControl = false
//    var startX = 0
//    var startY = 0
//    while (true) when (protoUnmarshal.readTag()) {
//        0 -> return UnitAdded(name, unitId, maxHp, playerControl,
//            startX, startY, protoUnmarshal.unknownFields())
//        10 -> name = protoUnmarshal.readString()
//        16 -> unitId = protoUnmarshal.readInt32()
//        24 -> maxHp = protoUnmarshal.readInt32()
//        32 -> playerControl = protoUnmarshal.readBool()
//        40 -> startX = protoUnmarshal.readInt32()
//        48 -> startY = protoUnmarshal.readInt32()
//        else -> protoUnmarshal.unknownField()
//    }
//}
//
//private fun UnitAdded.toJsonMapperImpl(): UnitAdded.JsonMapper =
//    UnitAdded.JsonMapper(
//        name.takeIf { it != "" },
//        unitId,
//        maxHp,
//        playerControl,
//        startX,
//        startY
//    )
//
//private fun UnitAdded.JsonMapper.toMessageImpl(): UnitAdded =
//    UnitAdded(
//        name = name ?: "",
//        unitId = unitId ?: 0,
//        maxHp = maxHp ?: 0,
//        playerControl = playerControl ?: false,
//        startX = startX ?: 0,
//        startY = startY ?: 0
//    )
//
//private fun UnitAdded.jsonMarshalImpl(json: Json): String =
//    json.stringify(UnitAdded.JsonMapper.serializer(), toJsonMapper())
//
//private fun UnitAdded.Companion.jsonUnmarshalImpl(json: Json, data: String): UnitAdded {
//    val mapper = json.parse(UnitAdded.JsonMapper.serializer(), data)
//    return mapper.toMessage()
//}
//
//fun Terrain?.orDefault() = this ?: Terrain.defaultInstance
//
//private fun Terrain.protoMergeImpl(plus: Terrain?): Terrain = plus?.copy(
//    unknownFields = unknownFields + plus.unknownFields
//) ?: this
//
//private fun Terrain.protoSizeImpl(): Int {
//    var protoSize = 0
//    if (type.value != 0) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.enumSize(type)
//    if (defenseTile) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.boolSize(defenseTile)
//    protoSize += unknownFields.entries.sumBy { it.value.size() }
//    return protoSize
//}
//
//private fun Terrain.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
//    if (type.value != 0) protoMarshal.writeTag(8).writeEnum(type)
//    if (defenseTile) protoMarshal.writeTag(16).writeBool(defenseTile)
//    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
//}
//
//private fun Terrain.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): Terrain {
//    var type: me.kumatheta.feh.message.Terrain.Type = me.kumatheta.feh.message.Terrain.Type.fromValue(0)
//    var defenseTile = false
//    while (true) when (protoUnmarshal.readTag()) {
//        0 -> return Terrain(type, defenseTile, protoUnmarshal.unknownFields())
//        8 -> type = protoUnmarshal.readEnum(me.kumatheta.feh.message.Terrain.Type.Companion)
//        16 -> defenseTile = protoUnmarshal.readBool()
//        else -> protoUnmarshal.unknownField()
//    }
//}
//
//private fun Terrain.toJsonMapperImpl(): Terrain.JsonMapper =
//    Terrain.JsonMapper(
//        type?.name,
//        defenseTile
//    )
//
//private fun Terrain.JsonMapper.toMessageImpl(): Terrain =
//    Terrain(
//        type = type?.let { me.kumatheta.feh.message.Terrain.Type.fromName(it) } ?: me.kumatheta.feh.message.Terrain.Type.fromValue(0),
//        defenseTile = defenseTile ?: false
//    )
//
//private fun Terrain.jsonMarshalImpl(json: Json): String =
//    json.stringify(Terrain.JsonMapper.serializer(), toJsonMapper())
//
//private fun Terrain.Companion.jsonUnmarshalImpl(json: Json, data: String): Terrain {
//    val mapper = json.parse(Terrain.JsonMapper.serializer(), data)
//    return mapper.toMessage()
//}
//
//fun BattleMapPosition?.orDefault() = this ?: BattleMapPosition.defaultInstance
//
//private fun BattleMapPosition.protoMergeImpl(plus: BattleMapPosition?): BattleMapPosition = plus?.copy(
//    terrain = terrain?.plus(plus.terrain) ?: plus.terrain,
//    unknownFields = unknownFields + plus.unknownFields
//) ?: this
//
//private fun BattleMapPosition.protoSizeImpl(): Int {
//    var protoSize = 0
//    if (x != 0) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.int32Size(x)
//    if (y != 0) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.int32Size(y)
//    if (terrain != null) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.messageSize(terrain)
//    if (obstacle != 0) protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.int32Size(obstacle)
//    protoSize += unknownFields.entries.sumBy { it.value.size() }
//    return protoSize
//}
//
//private fun BattleMapPosition.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
//    if (x != 0) protoMarshal.writeTag(8).writeInt32(x)
//    if (y != 0) protoMarshal.writeTag(16).writeInt32(y)
//    if (terrain != null) protoMarshal.writeTag(26).writeMessage(terrain)
//    if (obstacle != 0) protoMarshal.writeTag(32).writeInt32(obstacle)
//    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
//}
//
//private fun BattleMapPosition.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): BattleMapPosition {
//    var x = 0
//    var y = 0
//    var terrain: me.kumatheta.feh.message.Terrain? = null
//    var obstacle = 0
//    while (true) when (protoUnmarshal.readTag()) {
//        0 -> return BattleMapPosition(x, y, terrain, obstacle, protoUnmarshal.unknownFields())
//        8 -> x = protoUnmarshal.readInt32()
//        16 -> y = protoUnmarshal.readInt32()
//        26 -> terrain = protoUnmarshal.readMessage(me.kumatheta.feh.message.Terrain.Companion)
//        32 -> obstacle = protoUnmarshal.readInt32()
//        else -> protoUnmarshal.unknownField()
//    }
//}
//
//private fun BattleMapPosition.toJsonMapperImpl(): BattleMapPosition.JsonMapper =
//    BattleMapPosition.JsonMapper(
//        x,
//        y,
//        terrain?.toJsonMapper(),
//        obstacle
//    )
//
//private fun BattleMapPosition.JsonMapper.toMessageImpl(): BattleMapPosition =
//    BattleMapPosition(
//        x = x ?: 0,
//        y = y ?: 0,
//        terrain = terrain?.toMessage(),
//        obstacle = obstacle ?: 0
//    )
//
//private fun BattleMapPosition.jsonMarshalImpl(json: Json): String =
//    json.stringify(BattleMapPosition.JsonMapper.serializer(), toJsonMapper())
//
//private fun BattleMapPosition.Companion.jsonUnmarshalImpl(json: Json, data: String): BattleMapPosition {
//    val mapper = json.parse(BattleMapPosition.JsonMapper.serializer(), data)
//    return mapper.toMessage()
//}
//
//fun BattleMap?.orDefault() = this ?: BattleMap.defaultInstance
//
//private fun BattleMap.protoMergeImpl(plus: BattleMap?): BattleMap = plus?.copy(
//    battleMapPositions = battleMapPositions + plus.battleMapPositions,
//    unknownFields = unknownFields + plus.unknownFields
//) ?: this
//
//private fun BattleMap.protoSizeImpl(): Int {
//    var protoSize = 0
//    if (sizeX != 0) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.int32Size(sizeX)
//    if (sizeY != 0) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.int32Size(sizeY)
//    if (battleMapPositions.isNotEmpty()) protoSize += (pbandk.Sizer.tagSize(3) * battleMapPositions.size) + battleMapPositions.sumBy(pbandk.Sizer::messageSize)
//    protoSize += unknownFields.entries.sumBy { it.value.size() }
//    return protoSize
//}
//
//private fun BattleMap.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
//    if (sizeX != 0) protoMarshal.writeTag(8).writeInt32(sizeX)
//    if (sizeY != 0) protoMarshal.writeTag(16).writeInt32(sizeY)
//    if (battleMapPositions.isNotEmpty()) battleMapPositions.forEach { protoMarshal.writeTag(26).writeMessage(it) }
//    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
//}
//
//private fun BattleMap.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): BattleMap {
//    var sizeX = 0
//    var sizeY = 0
//    var battleMapPositions: pbandk.ListWithSize.Builder<me.kumatheta.feh.message.BattleMapPosition>? = null
//    while (true) when (protoUnmarshal.readTag()) {
//        0 -> return BattleMap(sizeX, sizeY, pbandk.ListWithSize.Builder.fixed(battleMapPositions), protoUnmarshal.unknownFields())
//        8 -> sizeX = protoUnmarshal.readInt32()
//        16 -> sizeY = protoUnmarshal.readInt32()
//        26 -> battleMapPositions = protoUnmarshal.readRepeatedMessage(battleMapPositions, me.kumatheta.feh.message.BattleMapPosition.Companion, true)
//        else -> protoUnmarshal.unknownField()
//    }
//}
//
//private fun BattleMap.toJsonMapperImpl(): BattleMap.JsonMapper =
//    BattleMap.JsonMapper(
//        sizeX,
//        sizeY,
//        battleMapPositions.map { it.toJsonMapper() }
//    )
//
//private fun BattleMap.JsonMapper.toMessageImpl(): BattleMap =
//    BattleMap(
//        sizeX = sizeX ?: 0,
//        sizeY = sizeY ?: 0,
//        battleMapPositions = battleMapPositions.map { it.toMessage() }
//    )
//
//private fun BattleMap.jsonMarshalImpl(json: Json): String =
//    json.stringify(BattleMap.JsonMapper.serializer(), toJsonMapper())
//
//private fun BattleMap.Companion.jsonUnmarshalImpl(json: Json, data: String): BattleMap {
//    val mapper = json.parse(BattleMap.JsonMapper.serializer(), data)
//    return mapper.toMessage()
//}
//
//fun SetupInfo?.orDefault() = this ?: SetupInfo.defaultInstance
//
//private fun SetupInfo.protoMergeImpl(plus: SetupInfo?): SetupInfo = plus?.copy(
//    battleMap = battleMap?.plus(plus.battleMap) ?: plus.battleMap,
//    unitAdded = unitAdded + plus.unitAdded,
//    unknownFields = unknownFields + plus.unknownFields
//) ?: this
//
//private fun SetupInfo.protoSizeImpl(): Int {
//    var protoSize = 0
//    if (battleMap != null) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.messageSize(battleMap)
//    if (unitAdded.isNotEmpty()) protoSize += (pbandk.Sizer.tagSize(2) * unitAdded.size) + unitAdded.sumBy(pbandk.Sizer::messageSize)
//    protoSize += unknownFields.entries.sumBy { it.value.size() }
//    return protoSize
//}
//
//private fun SetupInfo.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
//    if (battleMap != null) protoMarshal.writeTag(10).writeMessage(battleMap)
//    if (unitAdded.isNotEmpty()) unitAdded.forEach { protoMarshal.writeTag(18).writeMessage(it) }
//    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
//}
//
//private fun SetupInfo.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): SetupInfo {
//    var battleMap: me.kumatheta.feh.message.BattleMap? = null
//    var unitAdded: pbandk.ListWithSize.Builder<me.kumatheta.feh.message.UnitAdded>? = null
//    while (true) when (protoUnmarshal.readTag()) {
//        0 -> return SetupInfo(battleMap, pbandk.ListWithSize.Builder.fixed(unitAdded), protoUnmarshal.unknownFields())
//        10 -> battleMap = protoUnmarshal.readMessage(me.kumatheta.feh.message.BattleMap.Companion)
//        18 -> unitAdded = protoUnmarshal.readRepeatedMessage(unitAdded, me.kumatheta.feh.message.UnitAdded.Companion, true)
//        else -> protoUnmarshal.unknownField()
//    }
//}
//
//private fun SetupInfo.toJsonMapperImpl(): SetupInfo.JsonMapper =
//    SetupInfo.JsonMapper(
//        battleMap?.toJsonMapper(),
//        unitAdded.map { it.toJsonMapper() }
//    )
//
//private fun SetupInfo.JsonMapper.toMessageImpl(): SetupInfo =
//    SetupInfo(
//        battleMap = battleMap?.toMessage(),
//        unitAdded = unitAdded.map { it.toMessage() }
//    )
//
//private fun SetupInfo.jsonMarshalImpl(json: Json): String =
//    json.stringify(SetupInfo.JsonMapper.serializer(), toJsonMapper())
//
//private fun SetupInfo.Companion.jsonUnmarshalImpl(json: Json, data: String): SetupInfo {
//    val mapper = json.parse(SetupInfo.JsonMapper.serializer(), data)
//    return mapper.toMessage()
//}
//
//fun Action?.orDefault() = this ?: Action.defaultInstance
//
//private fun Action.protoMergeImpl(plus: Action?): Action = plus?.copy(
//    unknownFields = unknownFields + plus.unknownFields
//) ?: this
//
//private fun Action.protoSizeImpl(): Int {
//    var protoSize = 0
//    if (unitId != 0) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.int32Size(unitId)
//    if (moveX != 0) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.int32Size(moveX)
//    if (moveY != 0) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.int32Size(moveY)
//    if (targetUnitId != 0) protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.int32Size(targetUnitId)
//    protoSize += unknownFields.entries.sumBy { it.value.size() }
//    return protoSize
//}
//
//private fun Action.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
//    if (unitId != 0) protoMarshal.writeTag(8).writeInt32(unitId)
//    if (moveX != 0) protoMarshal.writeTag(16).writeInt32(moveX)
//    if (moveY != 0) protoMarshal.writeTag(24).writeInt32(moveY)
//    if (targetUnitId != 0) protoMarshal.writeTag(32).writeInt32(targetUnitId)
//    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
//}
//
//private fun Action.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): Action {
//    var unitId = 0
//    var moveX = 0
//    var moveY = 0
//    var targetUnitId = 0
//    while (true) when (protoUnmarshal.readTag()) {
//        0 -> return Action(unitId, moveX, moveY, targetUnitId, protoUnmarshal.unknownFields())
//        8 -> unitId = protoUnmarshal.readInt32()
//        16 -> moveX = protoUnmarshal.readInt32()
//        24 -> moveY = protoUnmarshal.readInt32()
//        32 -> targetUnitId = protoUnmarshal.readInt32()
//        else -> protoUnmarshal.unknownField()
//    }
//}
//
//private fun Action.toJsonMapperImpl(): Action.JsonMapper =
//    Action.JsonMapper(
//        unitId,
//        moveX,
//        moveY,
//        targetUnitId
//    )
//
//private fun Action.JsonMapper.toMessageImpl(): Action =
//    Action(
//        unitId = unitId ?: 0,
//        moveX = moveX ?: 0,
//        moveY = moveY ?: 0,
//        targetUnitId = targetUnitId ?: 0
//    )
//
//private fun Action.jsonMarshalImpl(json: Json): String =
//    json.stringify(Action.JsonMapper.serializer(), toJsonMapper())
//
//private fun Action.Companion.jsonUnmarshalImpl(json: Json, data: String): Action {
//    val mapper = json.parse(Action.JsonMapper.serializer(), data)
//    return mapper.toMessage()
//}
//
//fun UnitUpdate?.orDefault() = this ?: UnitUpdate.defaultInstance
//
//private fun UnitUpdate.protoMergeImpl(plus: UnitUpdate?): UnitUpdate = plus?.copy(
//    unknownFields = unknownFields + plus.unknownFields
//) ?: this
//
//private fun UnitUpdate.protoSizeImpl(): Int {
//    var protoSize = 0
//    if (unitId != 0) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.int32Size(unitId)
//    if (currentHp != 0) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.int32Size(currentHp)
//    if (active) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.boolSize(active)
//    protoSize += unknownFields.entries.sumBy { it.value.size() }
//    return protoSize
//}
//
//private fun UnitUpdate.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
//    if (unitId != 0) protoMarshal.writeTag(8).writeInt32(unitId)
//    if (currentHp != 0) protoMarshal.writeTag(16).writeInt32(currentHp)
//    if (active) protoMarshal.writeTag(24).writeBool(active)
//    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
//}
//
//private fun UnitUpdate.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): UnitUpdate {
//    var unitId = 0
//    var currentHp = 0
//    var active = false
//    while (true) when (protoUnmarshal.readTag()) {
//        0 -> return UnitUpdate(unitId, currentHp, active, protoUnmarshal.unknownFields())
//        8 -> unitId = protoUnmarshal.readInt32()
//        16 -> currentHp = protoUnmarshal.readInt32()
//        24 -> active = protoUnmarshal.readBool()
//        else -> protoUnmarshal.unknownField()
//    }
//}
//
//private fun UnitUpdate.toJsonMapperImpl(): UnitUpdate.JsonMapper =
//    UnitUpdate.JsonMapper(
//        unitId,
//        currentHp,
//        active
//    )
//
//private fun UnitUpdate.JsonMapper.toMessageImpl(): UnitUpdate =
//    UnitUpdate(
//        unitId = unitId ?: 0,
//        currentHp = currentHp ?: 0,
//        active = active ?: false
//    )
//
//private fun UnitUpdate.jsonMarshalImpl(json: Json): String =
//    json.stringify(UnitUpdate.JsonMapper.serializer(), toJsonMapper())
//
//private fun UnitUpdate.Companion.jsonUnmarshalImpl(json: Json, data: String): UnitUpdate {
//    val mapper = json.parse(UnitUpdate.JsonMapper.serializer(), data)
//    return mapper.toMessage()
//}
//
//fun UpdateInfo?.orDefault() = this ?: UpdateInfo.defaultInstance
//
//private fun UpdateInfo.protoMergeImpl(plus: UpdateInfo?): UpdateInfo = plus?.copy(
//    action = action?.plus(plus.action) ?: plus.action,
//    unitUpdates = unitUpdates + plus.unitUpdates,
//    unknownFields = unknownFields + plus.unknownFields
//) ?: this
//
//private fun UpdateInfo.protoSizeImpl(): Int {
//    var protoSize = 0
//    if (action != null) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.messageSize(action)
//    if (unitUpdates.isNotEmpty()) protoSize += (pbandk.Sizer.tagSize(2) * unitUpdates.size) + unitUpdates.sumBy(pbandk.Sizer::messageSize)
//    protoSize += unknownFields.entries.sumBy { it.value.size() }
//    return protoSize
//}
//
//private fun UpdateInfo.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
//    if (action != null) protoMarshal.writeTag(10).writeMessage(action)
//    if (unitUpdates.isNotEmpty()) unitUpdates.forEach { protoMarshal.writeTag(18).writeMessage(it) }
//    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
//}
//
//private fun UpdateInfo.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): UpdateInfo {
//    var action: me.kumatheta.feh.message.Action? = null
//    var unitUpdates: pbandk.ListWithSize.Builder<me.kumatheta.feh.message.UnitUpdate>? = null
//    while (true) when (protoUnmarshal.readTag()) {
//        0 -> return UpdateInfo(action, pbandk.ListWithSize.Builder.fixed(unitUpdates), protoUnmarshal.unknownFields())
//        10 -> action = protoUnmarshal.readMessage(me.kumatheta.feh.message.Action.Companion)
//        18 -> unitUpdates = protoUnmarshal.readRepeatedMessage(unitUpdates, me.kumatheta.feh.message.UnitUpdate.Companion, true)
//        else -> protoUnmarshal.unknownField()
//    }
//}
//
//private fun UpdateInfo.toJsonMapperImpl(): UpdateInfo.JsonMapper =
//    UpdateInfo.JsonMapper(
//        action?.toJsonMapper(),
//        unitUpdates.map { it.toJsonMapper() }
//    )
//
//private fun UpdateInfo.JsonMapper.toMessageImpl(): UpdateInfo =
//    UpdateInfo(
//        action = action?.toMessage(),
//        unitUpdates = unitUpdates.map { it.toMessage() }
//    )
//
//private fun UpdateInfo.jsonMarshalImpl(json: Json): String =
//    json.stringify(UpdateInfo.JsonMapper.serializer(), toJsonMapper())
//
//private fun UpdateInfo.Companion.jsonUnmarshalImpl(json: Json, data: String): UpdateInfo {
//    val mapper = json.parse(UpdateInfo.JsonMapper.serializer(), data)
//    return mapper.toMessage()
//}
