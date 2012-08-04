package dao

/**
 *
 * @author rodion
 */

trait DB[A] {
  def all: List[A]

  def save(item: A): A

  def update(item: A): A

  def delete(id: Long): A
}

object DB {
  val NEW_ID = -1L
}
