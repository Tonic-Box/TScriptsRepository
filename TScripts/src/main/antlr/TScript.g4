grammar TScript;

options {
  language = Java;
}

@header {
  package net.runelite.client.plugins.tscripts.adapter.lexer;
}

script : statement* EOF;

statement
    : functionDefinition
    | subscriberDefinition
    | variableDeclaration ';'
    | arrayDeclaration ';'
    | functionCall ';'
    | scopeStatement
    | block
    | ';'
    ;

scopeStatement
    : ifStatement
    | whileStatement
    | forStatement
    ;

ifStatement
    : 'if' '(' condition (glue condition)* ')' block ('else' block)?
    ;

whileStatement
    : 'while' '(' condition (glue condition)* ')' block
    ;

forStatement
    : 'for' '(' variableDeclaration ';' condition ';' variableDeclaration ')' block
    ;

condition
    : expression (comparator expression)?
    ;

block
    : '{' statement* '}'
    ;

functionDefinition
    : 'function' ID '(' params? ')' block
    ;

subscriberDefinition
    : 'subscribe' ID '(' array ')' block
    ;

variableDeclaration
    : variable assignmentOperator (expression | shorthandExpression)?
    ;

arrayDeclaration
    : array assignmentOperator (expression | shorthandExpression)?
    ;

variable
    : '$' ID
    ;

array
    : '$' ID '[' (expression | shorthandExpression)? ']'
    ;

comparator
    : '==' | '!=' | '<' | '<=' | '>' | '>='
    ;

glue
    : '&&' | '||'
    ;

unaryOperator
    : '-' | '!'
    ;

assignmentOperator
    : '-=' | '+=' | '=' | '++' | '--'
    ;

expression
    : '(' expression ')'
    | unaryOperator? '[' shorthandExpression ']'
    | unaryOperator? functionCall
    | unaryOperator? variable assignmentOperator?
    | unaryOperator? array
    | unaryOperator? NUMBER
    | unaryOperator? BOOLEAN
    | STRING
    | CONSTANT
    | 'null'
    ;

shorthandExpression
    : '(' shorthandExpression ')'
    | ternaryExpression
    | nullCoalescingExpression
    | nullCheck
    | unaryOperator?  '(' ternaryExpression ')'
    | unaryOperator? '(' nullCoalescingExpression ')'
    | unaryOperator? '(' nullCheck ')'
    ;

ternaryExpression
    : condition (glue condition)* '?' expression ':' expression
    ;

nullCoalescingExpression
    : expression '??' expression
    ;

nullCheck
    : expression '?'
    ;

functionCall
    : ID ('(' arguments? ')')?
    ;

params
    : (variable | array) (',' (variable | array))*
    ;

arguments
    : expression (',' expression)*
    ;

CONSTANT : [A-Z_]+;
STRING  : '"' (~["\\])* '"';
NUMBER  : [0-9]+ ('.' [0-9]+)?;
BOOLEAN : 'true' | 'false';
ID      : [a-zA-Z_] [a-zA-Z_0-9]*;
WS      : [ \t\r\n]+ -> skip;
COMMENT : '//' .*? '\n' -> skip;
BLOCK_COMMENT : '/*' .*? '*/' -> skip;