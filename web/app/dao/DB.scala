package dao

/**
 *
 * @author rodion
 */

trait DB[A] {
  def all: List[A]

  def save(item: A): A

  def delete(id: Long): A
}
