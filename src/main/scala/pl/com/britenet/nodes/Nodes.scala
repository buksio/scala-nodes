package pl.com.britenet.nodes

case class Node(id: Int, text: String, children: List[Node])

case class RawNode(id: Int, name: String, row: Int, col: Int)
