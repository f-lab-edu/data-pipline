package game.server.game.domain.monster.ai.bt

// Selector 노드: 자식들 중 하나라도 성공하면 성공
class SelectorNode(private val children: List<BTNode>) : BTNode {
    override fun tick(): Boolean {
        for (child in children) {
            if (child.tick()) return true
        }
        return false
    }
}

// Sequence 노드: 모든 자식이 순차적으로 성공해야 성공
class SequenceNode(private val children: List<BTNode>) : BTNode {
    override fun tick(): Boolean {
        for (child in children) {
            if (!child.tick()) return false
        }
        return true
    }
}

// 조건 노드: 조건을 평가하여 트리 진행 여부 결정
class ConditionNode(private val condition: () -> Boolean) : BTNode {
    override fun tick(): Boolean = condition()
}

// 행동 노드: 실제 행동 수행
class ActionNode(private val action: () -> Boolean) : BTNode {
    override fun tick(): Boolean = action()
}