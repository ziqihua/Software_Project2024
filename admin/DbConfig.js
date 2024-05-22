var mongoose = require('mongoose');
mongoose.set('useNewUrlParser', true);
mongoose.set('useFindAndModify', false);
mongoose.set('useCreateIndex', true);
mongoose.set('useUnifiedTopology', true);

// the host:port must match the location where you are running MongoDB
// the "myDatabase" part can be anything you like
mongoose.connect('mongodb://127.0.0.1:27017/myDatabase');

var Schema = mongoose.Schema;

var donationSchema = new Schema({
	contributor: String,
	fund: String,
	date: Date,
	amount: Number
    });

var fundSchema = new Schema({
	name: String,
	description: String,
	target: {type: Number, default: 0},
	org: String,
	donations: [donationSchema]
    });

var organizationSchema = new Schema({
	login: {type: String, required: true, unique: true},
	password: String,
	name: String,
	description: String,
	funds: [fundSchema]
    });

var contributorSchema = new Schema({
	login: {type: String, required: true, unique: true},
	password: String,
	name: String,
	email: String,
	creditCardNumber: String,
	creditCardCVV: String,
	creditCardExpiryMonth: {type: Number, default: 0},
	creditCardExpiryYear: {type: Number, default: 0},
	creditCardPostCode: String,
	donations: [donationSchema]
    });


const donationModel = mongoose.model('Donation', donationSchema);
const fundModel = mongoose.model('Fund', fundSchema);
const organizationModel = mongoose.model('Organization', organizationSchema);
const contributorModel = mongoose.model('Contributor', contributorSchema);

module.exports = {
    Donation : donationModel,
    Fund : fundModel,
    Organization: organizationModel,
    Contributor: contributorModel }
