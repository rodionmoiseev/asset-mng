package dao

import models.Persistent
import java.util.concurrent.{Callable, Executors}

/**
 *
 * @author rodion
 */

class SingleThreadedDB[A <: Persistent[A]](base: DB[A]) extends DB[A] {
  val es = Executors.newSingleThreadExecutor()

  private def syncTask[R](f: => R): R = {
    es.submit(new Callable[R] {
      def call() = f
    }).get
  }

  def all = syncTask {
    base.all
  }

  def save(item: A) = syncTask {
    base.save(item)
  }

  def update(item: A) = syncTask {
    base.update(item)
  }

  def delete(id: Long) = syncTask {
    base.delete(id)
  }
}
