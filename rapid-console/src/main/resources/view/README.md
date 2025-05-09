# Rapid Console Frontend

This is the frontend interface for the Rapid API Gateway Management Console.

## Structure

The frontend is built using:
- Vue.js (Vue 2)
- Element UI
- Axios for API requests
- Vue Router for routing

## Setup

1. The frontend is served directly from Spring Boot's static resources folder.
2. No build step is required since it uses Vue.js in non-build mode (direct script import).

## External Dependencies

The following external libraries should be downloaded and placed in the appropriate folders:

- Vue.js: `js/vue.min.js`
- Vue Router: `js/vue-router.min.js`
- Axios: `js/axios.min.js`
- Element UI: `js/element-ui.js` and `css/element-ui.css`

## Development

To modify the frontend:

1. Edit component files in the `js/components/` directory
2. Update routing in `js/main.js`
3. Modify styles in `css/main.css`

The main entry point is `index.html`, which loads all necessary scripts and stylesheets.

## API Endpoints

The frontend communicates with the following API endpoints:

- `/serviceDefinition/getList` - Get list of service definitions
- `/serviceInstance/getList` - Get service instances for a specific service
- `/serviceInstance/updateEnable` - Enable/disable a service instance
- `/serviceInstance/updateTags` - Update tags for a service instance
- `/serviceInstance/updateWeight` - Update weight for a service instance
- `/rule/getList` - Get list of rules
- `/rule/add` - Add a new rule
- `/rule/update` - Update an existing rule
- `/rule/delete` - Delete a rule 