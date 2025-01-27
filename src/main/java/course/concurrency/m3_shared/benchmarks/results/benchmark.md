# инструменты синхронизации данных

лучше всего использовать volatile_synchronized (volatile переменная с synchronized на запись)

|                       |   w-1-r-23 | w-12-r-12 |  w-23-r-1 | выводы                                                                                                                                                           |
|:---------------------:|-----------:|----------:|----------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
|       volatile        | 11 298 583 |   630 316 |    79 635 | отлично подходит для программ с преобладающим чтением                                                                                                            |
| volatile_synchronized | 10 675 111 | 7 257 156 | 1 865 766 | лучше всех подходит под любое соотношение читающих и пишущих потоков, немного проигрывает для случая преобладающего чтения                                       |
|     synchronized      |     18 154 |    17 255 |    20 120 | примерно одинаковая производительность для любого соотношения читающих и пишущих потоков, но лучше использовать другие инструменты с большей производительностью |
|        Atomic         |  8 169 222 |   338 614 |   120 777 | лучше подходит для программ с преобладающим чтением, в остальных случаях - резкая просадка производительности                                                    |
|   Atomic_optimistic   |    136 741 |    50 074 |    45 547 | лучше подходит для программ с преобладающим чтением, в остальных случаях - небольшая просадка производительности                                                 |

# многопоточные счетчики

для программ с преобладающим чтением или записью лучше всего использовать LongAdder_counter (переменная типа LongAdder)
для программ с равным кол-вом потоков чтения и записи, лучше всего использовать AtomicLong_counter (AtomicLong и
операциями incrementAndGet и get)

|                    |  w-1-r-23 | w-12-r-12 | w-23-r-1 | выводы                                                                                                                                            |
|:------------------:|----------:|----------:|---------:|:--------------------------------------------------------------------------------------------------------------------------------------------------|
| LongAdder_counter  | 7 127 642 |   127 120 |  699 950 | отлично показывает себя в приложениях с преобладающим чтением или записью, но имеет "просадку" для случая равного кол-ва поток на чтение и запись |
| AtomicLong_counter | 7 962 548 |   408 474 |   60 650 | лучше всех подходит для программ с преобладающим чтением, в остальных случаях - резкая просадка производительности                                |
| Atomic_accumulate  |   133 685 |    15 719 |   19 283 | имеет смысл использовать в программах с преобладающим чтением, но лучше использовать другие инструменты с большей производительностью             |

# инструменты доступа к ресурсам

любые fair блокировки имеют одинаковую производительность, но за счет низкой производительности не рекомендуются к
применению
лучше всего использовать StampedLock_optimistic (переменная с типом StampedLock и операциям tryOptimisticRead + validate
и writeLock + unlock)

|                        | w-1-r-23 | w-12-r-12 | w-23-r-1 | выводы                                                                                                                                       |
|:----------------------:|---------:|----------:|---------:|:---------------------------------------------------------------------------------------------------------------------------------------------|
|     ReentrantLock      |   45 653 |    43 850 |   44 690 | одинаковая производительность для любого соотношения читающих и пишущих потоков                                                              |
|   ReentrantLock_fair   |      250 |       250 |      252 | одинаковая производительность для любого соотношения читающих и пишущих потоков                                                              |
|     ReadWriteLock      |    5 986 |    24 106 |   41 686 | лучше подходит для программ с преобладающим чтением                                                                                          |
|   ReadWriteLock_fair   |      251 |       248 |      251 | одинаковая производительность для любого соотношения читающих и пишущих потоков                                                              |
| StampedLock_readwrite  |   17 047 |    36 665 |   46 970 | лучше подходит для программ с преобладающим чтением                                                                                          |
| StampedLock_optimistic |  375 669 |    48 585 |  252 672 | лучше всех подходит для программ с преобладающим чтением или записью, но имеет "просадку" для случая равного кол-ва поток на чтение и запись |
|       Semaphore        |   37 219 |    37 321 |   37 093 | одинаковая производительность для любого соотношения читающих и пишущих потоков                                                              |
|     Semaphore_fair     |      254 |       252 |      254 | одинаковая производительность для любого соотношения читающих и пишущих потоков                                                              |

---

# Общие выводы

- среди всех пессимистичных блокировок лучше всего использовать volatile_synchronized, так как там блокировка происходит только при записи
- среди пессимистичных блокировок-"объектов" лучше всех ReentrantLock
- среди оптимистичных блокировок - StampedLock_optimistic, но это аномалия, значит возьмем Atomic_optimistic
- если сравнивать между собой блокировки-"объекты", ReentrantLock и Atomic_optimistic, то я выберу - Atomic_optimistic так как для всех случаев нагрузки показывает лучшую производительность
- если можно и более "нативные", то - volatile_synchronized
- если синхронизация нужна для счетчика, то если потоков чтения и записи равное кол-во, то - AtomicLong_counter, а если распределение потоков не равное кол-во, то - LongAdder_counter
