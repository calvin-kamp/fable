// @ts-check
const eslint = require('@eslint/js')
const { defineConfig } = require('eslint/config')
const tseslint = require('typescript-eslint')
const angular = require('angular-eslint')
const prettier = require('eslint-config-prettier/flat')

module.exports = defineConfig([
	{
		ignores: ['dist/', '.angular/', 'coverage/'],
	},
	{
		files: ['**/*.ts'],
		languageOptions: {
			parserOptions: {
				projectService: true,
			},
		},
		extends: [
			eslint.configs.recommended,
			tseslint.configs.strictTypeChecked,
			tseslint.configs.stylisticTypeChecked,
			angular.configs.tsRecommended,
		],
		processor: angular.processInlineTemplates,
		rules: {
			'@angular-eslint/directive-selector': [
				'error',
				{ type: 'attribute', prefix: 'app', style: 'camelCase' },
			],
			'@angular-eslint/component-selector': [
				'error',
				{ type: ['element', 'attribute'], prefix: 'app', style: 'kebab-case' },
			],
			'@typescript-eslint/no-extraneous-class': ['error', { allowWithDecorator: true }],
		},
	},
	{
		files: ['**/*.html'],
		extends: [angular.configs.templateRecommended, angular.configs.templateAccessibility],
		rules: {},
	},
	prettier,
])
