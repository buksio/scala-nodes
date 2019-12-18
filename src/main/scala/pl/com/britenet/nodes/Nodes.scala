package pl.com.britenet.nodes

case class Node(id: Int, name: String, nodes: List[Node])

case class RawNode(id: Int, name: String, row: Int, col: Int)
