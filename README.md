# A Simple Remote Configurator (ASRC)

ASRC is a rule based configuration library that understands expressions like: `first_name == 'alice' && md5mod(USER_ID, 100) > 50`

It transforms a list of key-value based configurations (the input is often called [Context](#context)) into another configurations based on a list of pre-defined [formulas](#formula). 

To use this library, one can follow the instructions on [jitpack](https://jitpack.io/#shijinglu/asrc-core). For example, for maven projects:

```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
    <dependencies>
    	<dependency>
            <groupId>com.github.shijinglu</groupId>
            <artifactId>asrc-core</artifactId>
            <version>v0.1.4</version>
	    </dependency>
    </dependencies>
```


### Configurations
Similar to `java.util.Properties`, configurations are a list of key-value properties. Keys are always strings, Value objects can have different types: `Boolean`, `Integer`, `Double`, `String` or `Version`. Those five are build-in types. More data types can be added through the the [ExtensionManager](https://github.com/shijinglu/lure-java/blob/master/src/main/java/org/shijinglu/lure/extensions/ExtensionManager.java). All you need to do is to implement the interface [IData](https://github.com/shijinglu/lure-java/blob/master/src/main/java/org/shijinglu/lure/extensions/IData.java). And here is an example: [VersionData](https://github.com/shijinglu/lure-java/blob/master/src/main/java/org/shijinglu/lure/extensions/VersionData.java).


### Context
`Context` is alias for input configurations. Following is an example of context in JSON representation:

```json
{
    "PI": 3.14,
    "NATURAL_CONSTANT_E": 2.718,
    "first_name": "Alice",
    "last_name": "Liddell"
}
```


### Formula
In brief, `Formula` is a combination of [rule](#rule) attached with a value. It checks if the rule matches the context and if so, returns the associate value. In practice, the formula can have a tree structure and different values are attached to different tree nodes. In `ASRC` a yaml based formula is used as the default. As always, one can extend this to support customized formulas.

Following is an example of formula in YAML representation:

```yaml
- key: my_configuration
  value:
    - category: segment
      key: segment_0
      rule: PI >= 3
      value:
        - category: treatment
          key: treatment_group_00
          rule: NY_ZIP in (10001, 10002, 10003)
          value:
            - category: segment
              key: segment_000
              rule: NATURAL_CONSTANT_E < 0
              value: 0
            - category: segment
              key: segment_001
              rule: false
              value: 1001
        - category: treatment
          key: treatment_group_01
          rule: first_name == 'shijing'
          value: 1010
    - category: segment
      key: segment_1
      rule: last_name == 'Liddell'
      value:
        - category: treatment
          key: treatment_group_10
          rule: first_name != 'Alice'
          value: 110
        - category: treatment
          key: treatment_group_11
          value: 111
```

### Rule
A rule is a logical expression that can be evaluated to `true` or `false`. Evaluation of rules are provided by a package called `LURE`. 
