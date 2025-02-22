- 1.5.5

    - Support log on failure. See https://github.com/jlink/jqwik/issues/172 
  
    - Create possibility/annotation to suppress reporting inside of a
      test/property but not lose reporting of property's results. May require a
      new lifecycle hook.
        - Apply annotation wherever reporting is test collateral
        - Make it configurable, e.g. `jqwik.reporting.statistics.onfailureonly`

    - Arbitraries.forType(Class<T> targetType)
        - Recursive use
          - forType(Class<T> targetType, int depth)
          - @UseType(depth = 1)
        - See https://github.com/jlink/jqwik/issues/191

    - Add capability to easily generate java beans with Builders 
      (if that really makes sense).
      ```
      Beans.ofType(...)
             .excludeProperties(...)
             .use().forProperties(...)
      ```
      
    - Time Module (zinki97):
        - DateTimes. See https://github.com/jlink/jqwik/issues/175
        - Generate OffsetDateTime, Date, Calendar


- 1.5.x

    - Time Module:
        - <timebased>Arbitrary.shrinkTowards(date|time|dateTime)
        
    - Web Module:
        - Web.ipv4Addresses()|ipv6Addresses()|domains()|urls()

    - EdgeCases.Configuration.withProbability(double injectProbability)

    - `@Repeat(42)`: Repeat a property 42 times

    - Implement grow() for more shrinkables
        - CombinedShrinkable: grow each leg
        - CollectShrinkable: grow each element

    - Allow specification of provider class in `@ForAll` and `@From`
      see https://github.com/jlink/jqwik/issues/91

    - Allow state machine / model specification with temporal logic.
      See https://wickstrom.tech/programming/2021/05/03/specifying-state-machines-with-temporal-logic.html

    - Use derived Random object for generation of each parameter. Will that
      somehow break a random byte provider in guided generation?
        - Remember the random seed in Shrinkable

    - Guided Generation
      https://github.com/jlink/jqwik/issues/84
        - Maybe change AroundTryHook to allow replacement of `Random` source
        - Or: Introduce ProvideGenerationSourceHook

    - Allow to add frequency to chars for String and Character arbitraries eg.
      StringArbitrary.alpha(5).numeric(5).withChars("-", 1)

    - Mixin edge cases in random
      order (https://github.com/jlink/jqwik/issues/101)
    

